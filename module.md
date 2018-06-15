## cube-basic

```js
// 读取 instantId
readInstanceId(callback:Function)
// 用于大体积数据传输
putExtraData(httpUrl:String,params:Object)
// 关闭页面/弹窗
close()

// 打开页面
openUrl(webUrl:String)
// 打开弹窗
openDialog(webUrl:String, params:Object)
// 打开 web 页面
openWeb(webUrl:String)

// 加载 tab
loadTabs(array:Array)
// 显示 tab
showTab(tag:String)

// 注册事件
registerEvent(key:String)
// 发送事件
postEvent(key:String, params:Object)
// 取消注册事件
unRegisterEvent()
```

## cube-debug

```js
log(tag: String, msg: String)
logMsg(msg: String)
toast(msg: String)
toastLong(msg: String)
```

## cube-modal

```js
toast(msg: String)
toastLong(msg: String)
```

## cube-statusbar

```js
transluteStatusBar()
setStatusBarLight()
setStatusBarDark()
```