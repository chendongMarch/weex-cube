Manager 类均为单例模式，预注册到 ManagerRegistry


1. 监听 Weex 生命周期
2. 内部 放置数据


EnvManager        环境管理，切换开发、生产环境
DataManager       数据管理，针对 webUrl 暂存数据，解决页面数据传输问题
EventManager      事件管理，Weex 发送事件、处理事件中转战，解决多页面通信问题
HttpManager       请求管理，负责发起 Http 请求返回数据
WeexInstManager   Weex 实例管理