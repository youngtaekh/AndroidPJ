# CallClient

Developing libraries about **call**

# PJSIP

open-source

## Prerequisite

 1. Build pjproject-2.10
 2. Modify build.gradle(CallClient.pjsip)
	```gradle
	task copySo(type: Copy) {
		from '../../../pjproject-2.10/pjsip-apps/src/swig/java/android/app/src/main/jniLibs'
		into 'src/main/jniLibs'
		include('**/*.so')
	}
	task cpJava(type: Copy) {
		from '../../../pjproject-2.10/pjsip-apps/src/swig/java/android/app/src/main/java'
		into 'src/main/java'
		include('*/*/*/*.java')
	}
	tasks.withType(JavaCompile) { compileTask -> compileTask.dependsOn copySo }
	tasks.withType(JavaCompile) { compileTask -> compileTask.dependsOn cpJava }
	```
	```gradle
	sourceSets {
		main {
			jni.srcDirs = [
				"src/main/jni"
			]
		}
	}
	```
 3. Copy jni folder, pjsip java native folder