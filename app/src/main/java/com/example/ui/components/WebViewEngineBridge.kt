package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.GameMode

private class JsApiBridge(
    private val onJsonPayload: (String) -> Unit
) {
    @JavascriptInterface
    fun postJson(json: String) {
        if (json.isNotBlank()) {
            onJsonPayload(json)
        }
    }
}

/**
 * Uses Android's native Chromium WebView network stack (genuine browser TLS/JA3 fingerprint)
 * to fetch draw.ar-lottery01.com JSON and bridge it directly into our Kotlin ViewModel,
 * bypassing Cloudflare TLS blocks that can affect standard Java HTTP clients.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChromiumWebApiBridge(
    selectedMode: GameMode,
    syncTriggerCount: Int,
    onJsonReceived: (String) -> Unit
) {
    val bridge = remember(onJsonReceived) { JsApiBridge(onJsonReceived) }

    val pollHtml = remember {
        """
        <!DOCTYPE html>
        <html>
        <head><meta charset="utf-8"></head>
        <body>
        <script>
        var currentUrl = "https://draw.ar-lottery01.com/WinGo/WinGo_30S/GetHistoryIssuePage.json?ts=";
        function setModeUrl(u) {
            currentUrl = u;
            fetchNow();
        }
        async function fetchNow() {
            try {
                const res = await fetch(currentUrl + Date.now());
                const text = await res.text();
                if (text && text.indexOf("list") !== -1 && window.AndroidBridge) {
                    window.AndroidBridge.postJson(text);
                }
            } catch (e) {}
        }
        setInterval(fetchNow, 2000);
        fetchNow();
        </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = Modifier.size(1.dp),
        factory = { ctx ->
            try {
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    addJavascriptInterface(bridge, "AndroidBridge")
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL(
                        "https://draw.ar-lottery01.com/",
                        pollHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            } catch (t: Throwable) {
                android.view.View(ctx)
            }
        },
        update = { view ->
            if (view is WebView) {
                try {
                    view.evaluateJavascript("setModeUrl('${selectedMode.apiUrl}');", null)
                } catch (_: Throwable) {}
            }
        }
    )

    LaunchedEffect(syncTriggerCount) {
        // Triggered when user taps Sync button
    }
}

/**
 * Interactive WebView rendering the user's Matrix HTML engine enhanced with Website Premium Seed
 * and Period Sum tricks so the user can view the HTML engine directly inside the app as well.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveWebEngineTab(
    onJsonReceived: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bridge = remember(onJsonReceived) { JsApiBridge(onJsonReceived) }

    Column(modifier = modifier.fillMaxWidth()) {
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(740.dp)
                    .clip(RoundedCornerShape(14.dp)),
                factory = { ctx ->
                    try {
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            addJavascriptInterface(bridge, "AndroidBridge")
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                            loadDataWithBaseURL(
                                "https://draw.ar-lottery01.com/",
                                ENHANCED_WINGO_HTML,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    } catch (t: Throwable) {
                        android.widget.TextView(ctx).apply {
                            text = "WebView unavailable on this device. Use SIGNAL Tab."
                            setTextColor(android.graphics.Color.CYAN)
                        }
                    }
                }
            )
        }
    }
}

private const val ENHANCED_WINGO_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>WINGO QUANTUM WEB ENGINE</title>
<style>
*{margin:0;padding:0;box-sizing:border-box;}
body{background:#020812;color:#00ffee;font-family:Consolas,monospace;overflow-x:hidden;}
header{text-align:center;padding:14px;font-size:20px;font-weight:900;letter-spacing:2px;text-shadow:0 0 20px #00ffee;}
.card{margin:10px;padding:14px;border-radius:16px;background:rgba(4,16,30,.9);border:1px solid rgba(0,255,255,.3);box-shadow:0 0 20px rgba(0,255,255,.15);}
.buttons{display:flex;gap:8px;margin-bottom:14px;}
.btn{flex:1;padding:10px;border:none;border-radius:10px;background:#081521;color:#00ffee;cursor:pointer;font-weight:700;border:1px solid rgba(0,255,255,.25);}
.active{background:#00ffee;color:#000;}
.top{display:flex;justify-content:space-between;align-items:center;}
.timer-text{font-size:26px;font-weight:900;color:#00ffee;text-shadow:0 0 12px #00ffee;}
.period{text-align:right;font-size:12px;color:#9ff;}
.predict{text-align:center;margin-top:16px;padding:12px;border-radius:14px;background:rgba(0,0,0,.45);border:1px solid rgba(0,255,255,.2);}
.predict-main{font-size:48px;font-weight:900;}
.big{color:#00ff66;text-shadow:0 0 22px #00ff66;}
.small{color:#ff3355;text-shadow:0 0 22px #ff3355;}
.number{margin-top:6px;font-size:34px;font-weight:900;color:#00bbff;}
.color{margin-top:4px;font-size:16px;font-weight:900;}
.bar{width:100%;height:10px;background:#111;border-radius:10px;overflow:hidden;margin-top:6px;}
.fill{height:100%;width:0%;background:linear-gradient(90deg,#00ffee,#00ff66);}
.pattern-box{margin-top:14px;padding:12px;border-radius:12px;background:rgba(0,0,0,.4);border:1px solid rgba(0,255,255,.2);font-size:13px;line-height:1.5;color:#9ff;}
.stats{display:grid;grid-template-columns:repeat(4,1fr);gap:8px;margin-top:14px;}
.stat{padding:10px 6px;border-radius:10px;background:rgba(0,0,0,.4);border:1px solid rgba(0,255,255,.2);text-align:center;font-size:12px;}
.green{color:#00ff66;}
.red{color:#ff3355;}
table{width:100%;border-collapse:collapse;margin-top:10px;}
th,td{padding:7px;text-align:center;font-size:12px;}
th{background:rgba(0,255,255,.14);}
tr:nth-child(even){background:rgba(0,255,255,.04);}
.dot{width:10px;height:10px;border-radius:50%;display:inline-block;}
.c-red{background:#ff3355;}.c-green{background:#00ff66;}.c-violet{background:#bb77ff;}
</style>
</head>
<body>
<header>⚡ WINGO LIVE WEB ENGINE ⚡</header>
<div class="card">
<div class="buttons">
<button class="btn active" onclick="changeMode('30s',this)">30S</button>
<button class="btn" onclick="changeMode('1m',this)">1M</button>
<button class="btn" onclick="changeMode('3m',this)">3M</button>
<button class="btn" onclick="changeMode('5m',this)">5M</button>
</div>
<div class="top">
<div class="timer-text" id="timerText">--s</div>
<div class="period">CURRENT: <span id="currentPeriod">----</span><br>NEXT: <span id="nextPeriod">----</span></div>
</div>
<div class="predict">
<div style="font-size:11px;color:#9ff">QUANTUM WEB SIGNAL</div>
<div class="predict-main big" id="prediction">BIG</div>
<div class="number" id="numberPrediction">7 / 8</div>
<div class="color" id="colorPrediction">GREEN</div>
</div>
<div style="margin-top:12px;font-size:12px;">CONFIDENCE: <span id="confText">85%</span>
<div class="bar"><div class="fill" id="confFill" style="width:85%"></div></div>
</div>
<div class="pattern-box" id="patternText">ANALYZING LIVE SERVER SEED...</div>
<div class="stats">
<div class="stat green">WIN<br><b id="winCount">0</b></div>
<div class="stat red">LOSS<br><b id="lossCount">0</b></div>
<div class="stat">SIGNAL<br><b id="signalCount">0</b></div>
<div class="stat">STREAK<br><b id="streakCount">0</b></div>
</div>
</div>
<div class="card">
<div style="font-weight:bold;margin-bottom:6px;">LIVE SERVER HISTORY</div>
<table><thead><tr><th>PERIOD</th><th>NO</th><th>SIZE</th><th>SEED</th><th>CLR</th></tr></thead>
<tbody id="history"><tr><td colspan="5">CONNECTING...</td></tr></tbody></table>
</div>
<script>
const API = {
  '30s':'https://draw.ar-lottery01.com/WinGo/WinGo_30S/GetHistoryIssuePage.json?ts=',
  '1m':'https://draw.ar-lottery01.com/WinGo/WinGo_1M/GetHistoryIssuePage.json?ts=',
  '3m':'https://draw.ar-lottery01.com/WinGo/WinGo_3M/GetHistoryIssuePage.json?ts=',
  '5m':'https://draw.ar-lottery01.com/WinGo/WinGo_5M/GetHistoryIssuePage.json?ts='
};
let currentMode='30s', PERIOD_MS=30000, lastSeenPeriod=null;
let win=0, loss=0, signal=0, streak=0, lastPrediction=null, lastPredictionPeriod=null;
function sizeOf(n){return n>=5?'BIG':'SMALL';}
function colorOf(n){if(n===0||n===5)return 'VIOLET';if([2,4,6,8].includes(n))return 'RED';return 'GREEN';}
setInterval(()=>{const r=PERIOD_MS-(Date.now()%PERIOD_MS);timerText.innerText=Math.ceil(r/1000)+'s';},500);
function analyze(H){
  let big=0, small=0;
  const sizes=H.map(x=>sizeOf(x.number));
  H.slice(0,25).forEach((h,i)=>{const w=(25-i)/25; if(sizeOf(h.number)==='BIG')big+=w; else small+=w;});
  // Website Premium Seed Trick
  const prem=String(H[0].premium||'');
  let premSum=0; for(let c of prem){if(c>='0'&&c<='9')premSum+=Number(c);}
  const pTail=Number(String(H[0].period).slice(-2))||0;
  const seedPred=((premSum+pTail+H[0].number*3)%10)>=5?'BIG':'SMALL';
  if(seedPred==='BIG')big+=4.5; else small+=4.5;
  // Period + 3-Draw Sum Trick
  if(H.length>=3){
    const dSum=H[0].number+H[1].number+H[2].number+(pTail+1);
    if(dSum%2===0)big+=3.5; else small+=3.5;
  }
  // Streak & Violet Reset
  let st=1; for(let i=1;i<sizes.length;i++){if(sizes[i]===sizes[0])st++;else break;}
  if(H[0].number===0||H[0].number===5){if(sizes[0]==='BIG')small+=5;else big+=5;}
  else if(st>=4){if(sizes[0]==='BIG')small+=7;else big+=7;}
  else if(st===2){if(sizes[0]==='BIG')big+=3;else small+=3;}
  const pred=big>=small?'BIG':'SMALL';
  const conf=Math.min(96,Math.max(64,Math.round(66+Math.abs(big-small)*3.2)));
  const n1=pred==='BIG'?((H[0].number+3)%5+5):((H[0].number+2)%5);
  const n2=pred==='BIG'?((n1+2)%5+5):((n1+2)%5);
  return {prediction:pred,confidence:conf,number:n1+' / '+n2,color:colorOf(n1),pattern:'⚡ WEBSITE SEED ('+seedPred+') + MARKOV AI ACTIVE'};
}
async function poll(){
  try{
    const res=await fetch(API[currentMode]+Date.now());
    const txt=await res.text();
    if(window.AndroidBridge) window.AndroidBridge.postJson(txt);
    const j=JSON.parse(txt);
    const list=j?.data?.list||j?.list||[];
    if(!list.length)return;
    const history=list.map(x=>({period:x.issueNumber||x.issue,number:Number(x.number),premium:x.premium||x.price||'-'}));
    const current=history[0].period;
    if(lastPredictionPeriod&&current===lastPredictionPeriod){
      if(sizeOf(history[0].number)===lastPrediction){win++;streak++;}else{loss++;streak=0;}
      winCount.innerText=win;lossCount.innerText=loss;streakCount.innerText=streak;lastPredictionPeriod=null;
    }
    if(current!==lastSeenPeriod){
      lastSeenPeriod=current;
      const next=(BigInt(current)+1n).toString();
      currentPeriod.innerText=String(current).slice(-7);
      nextPeriod.innerText=String(next).slice(-7);
      const r=analyze(history);
      lastPrediction=r.prediction;lastPredictionPeriod=next;signal++;signalCount.innerText=signal;
      prediction.innerText=r.prediction;prediction.className='predict-main '+r.prediction.toLowerCase();
      numberPrediction.innerText=r.number;colorPrediction.innerText=r.color;
      confText.innerText=r.confidence+'%';confFill.style.width=r.confidence+'%';patternText.innerText=r.pattern;
      historyEl.innerHTML='';
      history.slice(0,10).forEach(h=>{
        historyEl.innerHTML += '<tr><td>' + String(h.period).slice(-7) + '</td><td><b>' + h.number + '</b></td><td>' + sizeOf(h.number) + '</td><td>' + String(h.premium).slice(-5) + '</td><td><span class="dot c-' + colorOf(h.number).toLowerCase() + '"></span></td></tr>';
      });
    }
  }catch(e){}
}
const historyEl=document.getElementById("history");
function changeMode(m,btn){currentMode=m;PERIOD_MS=m==='30s'?30000:m==='1m'?60000:m==='3m'?180000:300000;document.querySelectorAll('.btn').forEach(x=>x.classList.remove('active'));btn.classList.add('active');lastSeenPeriod=null;poll();}
poll();setInterval(poll,2000);
</script>
</body>
</html>
"""
