//
// Created by young on 2022/03/08.
//

#include "pj/log.h"
#include "pjsua2.hpp"
#include "pjmedia/sdp.h"
#include "pjsua-lib/pjsua.h"
#include <string>

PJ_DEF(pj_status_t)
pjsua_call_answer_with_sdp(pjsua_call_id call_id,
			    const pjmedia_sdp_session *sdp,
			    const pjsua_call_setting *opt,
			    unsigned code,
			    const pj_str_t *reason,
			    const pjsua_msg_data *msg_data)
{
    pjsua_call *call;
    pjsip_dialog *dlg = NULL;
    pj_status_t status;

    PJ_ASSERT_RETURN(call_id>=0 && call_id<(int)pjsua_var.ua_cfg.max_calls,
		     PJ_EINVAL);

    status = acquire_call("pjsua_call_answer_with_sdp()",
    			  call_id, &call, &dlg);
    if (status != PJ_SUCCESS)
	return status;

    status = pjsip_inv_set_sdp_answer(call->inv, sdp);

    pjsip_dlg_dec_lock(dlg);

    if (status != PJ_SUCCESS)
    	return status;

    return pjsua_call_answer2(call_id, opt, code, reason, msg_data);
}

PJ_DEF(pj_status_t)
pjsua_call_answer2(pjsua_call_id call_id,
                const pjsua_call_setting *opt,
                unsigned code,
                const pj_str_t *reason,
                const pjsua_msg_data *msg_data)
{
    pjsua_call *call;
    pjsip_dialog *dlg = NULL;
    pjsip_tx_data *tdata;
    pj_status_t status;

    PJ_ASSERT_RETURN(call_id>=0 && call_id<(int)pjsua_var.ua_cfg.max_calls,
		     PJ_EINVAL);

    PJ_LOG(4,(THIS_FILE, "Answering call %d: code=%d", call_id, code));
    pj_log_push_indent();

    status = acquire_call("pjsua_call_answer()", call_id, &call, &dlg);
    if (status != PJ_SUCCESS)
	goto on_return;

    /* Apply call setting, only if status code is 1xx or 2xx. */
    if (opt && code < 300) {
	/* Check if it has not been set previously or it is different to
	 * the previous one.
	 */
	if (!call->opt_inited) {
	    call->opt_inited = PJ_TRUE;
	    apply_call_setting(call, opt, NULL);
	} else if (pj_memcmp(opt, &call->opt, sizeof(*opt)) != 0) {
	    /* Warn application about call setting inconsistency */
	    PJ_LOG(2,(THIS_FILE, "The call setting changes is ignored."));
	}
    }

    PJSUA_LOCK();

    /* Ticket #1526: When the incoming call contains no SDP offer, the media
     * channel may have not been initialized at this stage. The media channel
     * will be initialized here (along with SDP local offer generation) when
     * the following conditions are met:
     * - no pending media channel init
     * - local SDP has not been generated
     * - call setting has just been set, or SDP offer needs to be sent, i.e:
     *   answer code 183 or 2xx is issued
     */
    if (!call->med_ch_cb &&
	(call->opt_inited || (code==183 || code/100==2)) &&
	(!call->inv->neg ||
	 pjmedia_sdp_neg_get_state(call->inv->neg) ==
		PJMEDIA_SDP_NEG_STATE_NULL))
    {
	/* Mark call setting as initialized as it is just about to be used
	 * for initializing the media channel.
	 */
	call->opt_inited = PJ_TRUE;

	status = pjsua_media_channel_init(call->index, PJSIP_ROLE_UAC,
					  call->secure_level,
					  dlg->pool,
					  NULL, NULL, PJ_TRUE,
					  &on_answer_call_med_tp_complete);
	if (status == PJ_SUCCESS) {
	    status = on_answer_call_med_tp_complete(call->index, NULL);
	    if (status != PJ_SUCCESS) {
		PJSUA_UNLOCK();
		goto on_return;
	    }
	} else if (status != PJ_EPENDING) {
	    PJSUA_UNLOCK();
	    pjsua_perror(THIS_FILE, "Error initializing media channel", status);
	    goto on_return;
	}
    }

    /* If media transport creation is not yet completed, we will answer
     * the call in the media transport creation callback instead.
     * Or if initial answer is not sent yet, we will answer the call after
     * initial answer is sent (see #1923).
     */
    if (call->med_ch_cb || !call->inv->last_answer) {
        struct call_answer *answer;

        PJ_LOG(4,(THIS_FILE, "Pending answering call %d upon completion "
                             "of media transport", call_id));

        answer = PJ_POOL_ZALLOC_T(call->inv->pool_prov, struct call_answer);
        answer->code = code;
	if (opt) {
	    answer->opt = PJ_POOL_ZALLOC_T(call->inv->pool_prov,
					   pjsua_call_setting);
	    *answer->opt = *opt;
	}
        if (reason) {
	    answer->reason = PJ_POOL_ZALLOC_T(call->inv->pool_prov, pj_str_t);
            pj_strdup(call->inv->pool_prov, answer->reason, reason);
        }
        if (msg_data) {
            answer->msg_data = pjsua_msg_data_clone(call->inv->pool_prov,
                                                    msg_data);
        }
        pj_list_push_back(&call->async_call.call_var.inc_call.answers,
                          answer);

        PJSUA_UNLOCK();
        if (dlg) pjsip_dlg_dec_lock(dlg);
        pj_log_pop_indent();
        return status;
    }

    PJSUA_UNLOCK();

    if (call->res_time.sec == 0)
	pj_gettimeofday(&call->res_time);

    if (reason && reason->slen == 0)
	reason = NULL;

    /* Create response message */
    status = pjsip_inv_answer(call->inv, code, reason, NULL, &tdata);
    if (status != PJ_SUCCESS) {
	pjsua_perror(THIS_FILE, "Error creating response",
		     status);
	goto on_return;
    }

    /* Call might have been disconnected if application is answering with
     * 200/OK and the media failed to start.
     */
    if (call->inv == NULL)
	goto on_return;

    /* Add additional headers etc */
    pjsua_process_msg_data( tdata, msg_data);

    /* Send the message */
    status = pjsip_inv_send_msg(call->inv, tdata);
    if (status != PJ_SUCCESS)
	pjsua_perror(THIS_FILE, "Error sending response",
		     status);

on_return:
    if (dlg) pjsip_dlg_dec_lock(dlg);
    pj_log_pop_indent();
    return status;
}

PJ_DEF(pj_status_t) pjsua_call_make_call(pjsua_acc_id acc_id,
const pj_str_t *dest_uri,
const pjsua_call_setting *opt,
void *user_data,
const pjsua_msg_data *msg_data,
        pjsua_call_id *p_call_id)
{
    pj_pool_t *tmp_pool = NULL;
    pjsip_dialog *dlg = NULL;
    pjsua_acc *acc;
    pjsua_call *call;
    int call_id = -1;
    pj_str_t contact;
    pj_status_t status;

    /* Check that account is valid */
    PJ_ASSERT_RETURN(acc_id>=0 || acc_id<(int)PJ_ARRAY_SIZE(pjsua_var.acc),
            PJ_EINVAL);

    /* Check arguments */
    PJ_ASSERT_RETURN(dest_uri, PJ_EINVAL);

    PJ_LOG(4,(THIS_FILE, "Making call with acc #%d to %.*s", acc_id,
    (int)dest_uri->slen, dest_uri->ptr));

    pj_log_push_indent();

    PJSUA_LOCK();

    acc = &pjsua_var.acc[acc_id];
    if (!acc->valid) {
    pjsua_perror(THIS_FILE, "Unable to make call because account "
    "is not valid", PJ_EINVALIDOP);
    status = PJ_EINVALIDOP;
    goto on_error;
    }

    /* Find free call slot. */
    call_id = alloc_call_id();

    if (call_id == PJSUA_INVALID_ID) {
    pjsua_perror(THIS_FILE, "Error making call", PJ_ETOOMANY);
    status = PJ_ETOOMANY;
    goto on_error;
    }

    /* Clear call descriptor */
    reset_call(call_id);

    call = &pjsua_var.calls[call_id];

    /* Associate session with account */
    call->acc_id = acc_id;
    call->call_hold_type = acc->cfg.call_hold_type;

    /* Generate per-session RTCP CNAME, according to RFC 7022. */
    pj_create_random_string(call->cname_buf, call->cname.slen);

    /* Apply call setting */
    status = apply_call_setting(call, opt, NULL);
    if (status != PJ_SUCCESS) {
    pjsua_perror(THIS_FILE, "Failed to apply call setting", status);
    goto on_error;
    }

    /* Create sound port if none is instantiated, to check if sound device
     * can be used. But only do this with the conference bridge, as with
     * audio switchboard (i.e. APS-Direct), we can only open the sound
     * device once the correct format has been known
     */
    if (!pjsua_var.is_mswitch && pjsua_var.snd_port==NULL &&
    pjsua_var.null_snd==NULL && !pjsua_var.no_snd && call->opt.aud_cnt > 0)
    {
    //audio setting
    PJ_LOG(4, (THIS_FILE, "audio setting"));
    //status = pjsua_set_snd_dev(pjsua_var.cap_dev, pjsua_var.play_dev);
    //if (status != PJ_SUCCESS)
    //goto on_error;
    }

    /* Create temporary pool */
    tmp_pool = pjsua_pool_create("tmpcall10", 512, 256);

    /* Verify that destination URI is valid before calling
     * pjsua_acc_create_uac_contact, or otherwise there
     * a misleading "Invalid Contact URI" error will be printed
     * when pjsua_acc_create_uac_contact() fails.
     */
    if (1) {
    pjsip_uri *uri;
    pj_str_t dup;

    pj_strdup_with_null(tmp_pool, &dup, dest_uri);
    uri = pjsip_parse_uri(tmp_pool, dup.ptr, dup.slen, 0);

    if (uri == NULL) {
    pjsua_perror(THIS_FILE, "Unable to make call",
    PJSIP_EINVALIDREQURI);
    status = PJSIP_EINVALIDREQURI;
    goto on_error;
    }
    }

    /* Mark call start time. */
    pj_gettimeofday(&call->start_time);

    /* Reset first response time */
    call->res_time.sec = 0;

    /* Create suitable Contact header unless a Contact header has been
     * set in the account.
     */
    if (acc->contact.slen) {
        contact = acc->contact;
    } else {
        status = pjsua_acc_create_uac_contact(tmp_pool, &contact,
                                              acc_id, dest_uri);
        if (status != PJ_SUCCESS) {
            pjsua_perror(THIS_FILE, "Unable to generate Contact header",
            status);
            goto on_error;
        }
    }

    /* Create outgoing dialog: */
    status = pjsip_dlg_create_uac( pjsip_ua_instance(),
                                   &acc->cfg.id, &contact,
                                   dest_uri,
                                   (msg_data && msg_data->target_uri.slen?
                                    &msg_data->target_uri: dest_uri),
                                   &dlg);
    if (status != PJ_SUCCESS) {
        pjsua_perror(THIS_FILE, "Dialog creation failed", status);
        goto on_error;
    }

    /* Increment the dialog's lock otherwise when invite session creation
     * fails the dialog will be destroyed prematurely.
     */
    pjsip_dlg_inc_lock(dlg);

    dlg_set_via(dlg, acc);

    /* Calculate call's secure level */
    call->secure_level = get_secure_level(acc_id, dest_uri);

    /* Attach user data */
    call->user_data = user_data;

    /* Store variables required for the callback after the async
     * media transport creation is completed.
     */
    if (msg_data) {
        call->async_call.call_var.out_call.msg_data = pjsua_msg_data_clone(
                dlg->pool, msg_data);
    }
    call->async_call.dlg = dlg;

    /* Temporarily increment dialog session. Without this, dialog will be
     * prematurely destroyed if dec_lock() is called on the dialog before
     * the invite session is created.
     */
    pjsip_dlg_inc_session(dlg, &pjsua_var.mod);

    if ((call->opt.flag & PJSUA_CALL_NO_SDP_OFFER) == 0) {
        /* Init media channel */
        status = pjsua_media_channel_init(call->index, PJSIP_ROLE_UAC,
                                          call->secure_level, dlg->pool,
                                          NULL, NULL, PJ_TRUE,
                                          &on_make_call_med_tp_complete);
    }
    if (status == PJ_SUCCESS) {
        status = on_make_call_med_tp_complete(call->index, NULL);
        if (status != PJ_SUCCESS)
            goto on_error;
    } else if (status != PJ_EPENDING) {
        pjsua_perror(THIS_FILE, "Error initializing media channel", status);
        pjsip_dlg_dec_session(dlg, &pjsua_var.mod);
        goto on_error;
    }

    /* Done. */

    if (p_call_id)
    *p_call_id = call_id;

    pjsip_dlg_dec_lock(dlg);
    pj_pool_release(tmp_pool);
    PJSUA_UNLOCK();

    pj_log_pop_indent();

    return PJ_SUCCESS;

on_error:
    if (dlg) {
    /* This may destroy the dialog */
    pjsip_dlg_dec_lock(dlg);
    }

    if (call_id != -1) {
    pjsua_media_channel_deinit(call_id);
    reset_call(call_id);
    }

    pjsua_check_snd_dev_idle();

    if (tmp_pool)
    pj_pool_release(tmp_pool);
    PJSUA_UNLOCK();

    pj_log_pop_indent();
    return status;
}

PJ_DEF(pj_status_t) pjmedia_sdp_neg_set_local_answer( pj_pool_t *pool,
				  pjmedia_sdp_neg *neg,
				  const pjmedia_sdp_session *local)
{
    /* Check arguments are valid. */
    PJ_ASSERT_RETURN(pool && neg && local, PJ_EINVAL);

    /* Can only do this in STATE_REMOTE_OFFER or WAIT_NEGO.
     * If we already provide local offer, then set_remote_answer() should
     * be called instead of this function.
     */
    PJ_ASSERT_RETURN(neg->state == PJMEDIA_SDP_NEG_STATE_REMOTE_OFFER ||
    		     neg->state == PJMEDIA_SDP_NEG_STATE_WAIT_NEGO,
		     PJMEDIA_SDPNEG_EINSTATE);

    /* State now is STATE_WAIT_NEGO. */
    neg->state = PJMEDIA_SDP_NEG_STATE_WAIT_NEGO;
    if (local) {
	neg->neg_local_sdp = pjmedia_sdp_session_clone(pool, local);
	if (neg->initial_sdp) {
	    /* Retain initial_sdp value. */
	    neg->initial_sdp_tmp = neg->initial_sdp;
	    neg->initial_sdp = pjmedia_sdp_session_clone(pool,
							 neg->initial_sdp);

	    /* I don't think there is anything in RFC 3264 that mandates
	     * answerer to place the same origin (and increment version)
	     * in the answer, but probably it won't hurt either.
	     * Note that the version will be incremented in
	     * pjmedia_sdp_neg_negotiate()
	     */
	    neg->neg_local_sdp->origin.id = neg->initial_sdp->origin.id;
	} else {
	    neg->initial_sdp = pjmedia_sdp_session_clone(pool, local);
	}
    } else {
	PJ_ASSERT_RETURN(neg->initial_sdp, PJMEDIA_SDPNEG_ENOINITIAL);
	neg->initial_sdp_tmp = neg->initial_sdp;
	neg->initial_sdp = pjmedia_sdp_session_clone(pool, neg->initial_sdp);
	neg->neg_local_sdp = pjmedia_sdp_session_clone(pool, neg->initial_sdp);
    }

    return PJ_SUCCESS;
}