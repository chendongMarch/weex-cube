# Weex 容器支持 (Android、Kotlin)




## 完全 Weex 化

完全 Weex 化定制性要差一些，目前主要支持

1. 闪屏页替换


## 混合

如果有一定的 Android 开发基础就简单多了，可以自由的对容器进行扩展



webUrl  //www.web.test.com/home/test-weex.html 带 host 配置
configUrl //www.config.test.com/weex-config 就这一个，直接配死
reqUrl  //www.api.test.com/list   vue 端控制
jsUrl //www.assets.test.com/test-weex.js 最需要被调试，host 定制


10.1.2.105 == 10.1.2.105  8081 == 8081 && /channel/channel-list-weex == /wanandroid/wanandroid-category-weex
10.1.2.105 == 10.1.2.105  8081 == 8081 && /channel/channel-list-weex == /wanandroid/wanandroid-about-weex
10.1.2.105 == 10.1.2.105  8081 == 8081 && /channel/channel-list-weex == /wanandroid/wanandroid-tree-weex
10.1.2.105 == 10.1.2.105  8081 == 8081 && /channel/channel-list-weex == /wanandroid/wanandroid-recommend-weex
10.1.2.105 == 10.1.2.105  8081 == 8081 && /channel/channel-list-weex == /wanandroid/wanandroid-index-weex
10.1.2.105 == 10.1.2.105  8081 == 8081 && /channel/channel-list-weex == /art/art-home-weex