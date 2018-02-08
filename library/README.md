#edge-gateway-jar
===
edge-gateway公共逻辑, 包括透传, 访问日志, 流控等.  
类库生成工具: <a href="https://jitpack.io" target="_blank">https://jitpack.io</a>
## Gradle

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```groovy
	allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```groovy
	dependencies {
	        compile 'com.github.chuangxian.lib-edge:5db2f5c4be'
	}
```