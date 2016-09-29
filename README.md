# AndroidBaseLibrary [![Build Status](https://travis-ci.org/wealthworks/AndroidBaseLibrary.svg?branch=master)](https://travis-ci.org/wealthworks/AndroidBaseLibrary) [![jcenter](https://raw.githubusercontent.com/wealthworks/AndroidBaseLibrary/master/doc/jcenter.png)](https://bintray.com/licaigc/maven/androidbaselibrary)
我场 Android 平台基础库. 部分代码 forked from [WallE](https://github.com/walfud/WallE). 部分功能进行了定制和删改. 遵循 Apache 2.0 协议.

# Usage

app/**build.gradle:**

`compile 'com.licaigc:androidbaselibrary:+'`

建议各位在理解 [Semantic Versioning](http://semver.org/) 的前提下, 根据版本号合理规划引入方式. 我介绍几种常见的场景:

1. 需要某个稳定的功能: (适合大部分使用者)
`compile 'com.licaigc:androidbaselibrary:1.2.+'`
2. 希望使用最新加入的功能:
`compile 'com.licaigc:androidbaselibrary:1.+'`
3. 希望体验最新的功能和语法:
`compile 'com.licaigc:androidbaselibrary:+'`

app/**XxxApplication.java:**

```java
public class TimiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ...

        AndroidBaseLibrary.initialize(getApplicationContext());

        ...
    }
}
```

app/**build.gradle**

```java
apply plugin: 'com.android.application'

android {
    compileSdkVersion ...
    buildToolsVersion "..."

    defaultConfig {
        applicationId "..."
        minSdkVersion ...
        targetSdkVersion ...
        versionCode ...
        versionName "..."

        manifestPlaceholders = [
                JPUSH_PKGNAME : applicationId,          // 保持原样
                JPUSH_APPKEY  : "这里填写 JPush 上注册的包名对应的appkey",
                JPUSH_CHANNEL : "developer-default",    // 保持原样
        ]
    }
    buildTypes {
        release {
            ...
        }
    }
}

dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    ...
}
```

具体使用, 请见 [wiki](https://github.com/wealthworks/AndroidBaseLibrary/wiki).

# Trouble Shooting
1. 编译错误: _> NDK integration is deprecated in the current plugin.._

  A: 在 **app/gradle.properties**(如果没有则创建) 中增加 `android.useDeprecatedNdk=true`

# Branch Management
master: 作为主要开发分支.

tag: 作为发布分支.

# Coding Style
```java
public class Foo {
    public static final String TAG = "Foo";

    public static final int DEFAULT_VALUE = 0;
    private static final String PREFS_BAR = "...";

    private long mId;
    private TextView mNameTv;

    public Foo() {

    }
    private Foo(id) {
        mId = id;
    }

    // Function
    public void foo() {

    }

    public void bar() {

    }

    // internal
    private void baz() {

    }

    protected void xyz() {

    }

    // Helper
    private static Foo sInstance;
    public static Foo getInstance() {
        ...
    }
}
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="320dp"
              android:layout_height="wrap_content"
              android:gravity="center_horizontal"
              android:orientation="vertical"
              android:background="@android:color/white">

    <TextView
        android:id="@+id/tv_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="30dp"
        android:text="骚年，是时候更新了~"
        android:textSize="20sp"
        android:textColor="#FF4A4A4A"/>

</LinearLayout>
```
更详细的内容, 请看 [wiki](https://github.com/wealthworks/AndroidBaseLibrary/wiki/编码规范).

# Contribution
发起 PR, 我会尽快 Merge.
请注意, 新增功能请写注释, 并**在 [wiki](https://github.com/wealthworks/AndroidBaseLibrary/wiki) 中补充说明**.

# Maintainer
得明(songdeming@licaigc.com)

License
=======
    Copyright 2013 WealthWorks, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

# Powered by
[![licaigc](https://raw.githubusercontent.com/wealthworks/AndroidBaseLibrary/master/doc/licaigc.png)](http://www.talicai.com/)
