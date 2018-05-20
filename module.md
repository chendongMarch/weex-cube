## cube-basic

```js
readInstanceId(callback:Function)
putExtraData(url:String,params:Object)
close()

openUrl(webUrl:String)
openDialog(webUrl:String, params:Object)
openWeb(webUrl:String)

loadTabs(array:Array)
showTab(tag:String)

registerEvent(key:String)
postEvent(key:String, params:Object)
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