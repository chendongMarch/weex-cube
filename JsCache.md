

## 内置资源准备

为了提供良好的加载体验：

- 「强制」必须在 `assets` 中打包一份合适的配置文件，避免网络下载过慢或者无法下载的情况。
- 「建议」最好能在 assets 中打包一份 js 资源文件，在网络资源还没加载下来时，保证流畅渲染。

assets 目录结构

```
- assets
    - config
        - config.json
    - js
        - home-page-1-1-0.js
        ...
    - res
```

## Js 配置文件加载方案


```
启动时尝试从 Local(config.json) 读取数据，来源是 Net，保证配置文件最新
无法读取到则尝试从 Assets(config.json) 读取，保证读取速度，完毕后存储到 Local 中
启动配置文件下载，下载完毕后存储到 Local 供下次启动时读取
当配置文件读取完毕，检索首页跳转
```


## Js 资源加载方案

Js 的来源有 4 个地方

```
Cache   内存缓存
Assets  包内资源文件
Local   磁盘存储
Net     网络数据
```

访问速度 Cache > Assets > Local > Net

所有资源都使用 page + jsVersion 来唯一标记

```
构建 page + js 的 key
Cache
Assets
Local
Net(存储到 Local，加快下次访问)
```


加载策略（指的是为 Weex 页面加载 Js 的策略）

```kotlin
object JsLoadStrategy {
    const val NET_FIRST = 0 // 只加载网络
    const val FILE_FIRST = 1 // 只加载文件
    const val ASSETS_FIRST = 2 // 只加载 assets
    const val CACHE_FIRST = 3 // 只加载缓存
    const val DEFAULT = 4 // 默认 缓存、文件、assets、网络 一次检查
}
```

缓存策略（指的是 Js 向内存中缓存的的策略）

```kotlin
// 缓存策略
object JsCacheStrategy {
    const val PREPARE_ALL = 0 // 提前准备所有的js到缓存中
    const val LAZY_LOAD = 2 // 使用时才加载
    const val NO_CACHE = 3 // 不缓存
}
```
