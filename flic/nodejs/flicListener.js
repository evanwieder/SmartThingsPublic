var fliclib = require("/var/lib/fliclib/nodejs/fliclibNodeJs");
var FlicClient = fliclib.FlicClient;
var FlicConnectionChannel = fliclib.FlicConnectionChannel;
var FlicScanner = fliclib.FlicScanner;

var client = new FlicClient("localhost", 5551);
require('console-stamp')(console, '[HH:MM:ss.l]');

// REST connection params
var installationID = '<<installatonID here>>';
var accessToken = '<<accessToken here>>';


var connectionType='mqtt';
//var connectionType='mqtt';  // 'mqtt' or 'REST'

if (connectionType=='REST') {
	var https = require("https");
}
else { // mqtt
       var moscaSettings = {  port: 1883};
	// setup mosca server
	var mosca = require("mosca");
	var moscaServer = new mosca.Server(moscaSettings);
	moscaServer.on('ready', moscaServerStarted);
	
	//MQTT connection params
	
	var mqtt = require('mqtt');
	var mqttClient  = mqtt.connect('mqtt://localhost');
}



// Flic device names should be 'Flic - <Color> <Seq>': Flic - Black 2


var flicList = [
		{
		bAddr: '80:e4:da:71:8d:6f',
		color: 'turquoise',
		colorSeq: '1'
	}, {
		bAddr: '80:e4:da:71:be:cb',
		color: 'green',
		colorSeq: '1'
	}, {
		bAddr: '80:e4:da:71:ab:7d',
		color: 'white',
		colorSeq: '1'
	}, {
		bAddr: '80:e4:da:70:58:35',
		color: 'white',
		colorSeq: '2'
	}, {
		bAddr: '80:e4:da:70:53:64',
		color: 'white',
		colorSeq: '3'
	}, {
		bAddr: '80:e4:da:70:58:24',
		color: 'white',
		colorSeq: '4'
	}, {
		bAddr: '80:e4:da:71:91:da',
		color: 'black',
		colorSeq: '1'
	}, {
		bAddr: '80:e4:da:70:1c:63',
		color: 'black',
		colorSeq: '2'
	}
];




var clickTypeMap = [{
	clickTypeIn: 'ButtonSingleClick',
	clickTypeOut: 'click'
}, {
	clickTypeIn: 'ButtonDoubleClick',
	clickTypeOut: 'doubleClick'
}, {
	clickTypeIn: 'ButtonHold',
	clickTypeOut: 'hold'
}, ];

var connectionOptions = {
	latencyMode: "NormalLatency",
	autoDisconnectTime: "180"
};

var maxQueuedTimeSecs = 5;


function listenToButton(bdAddr) {
	var cc = new FlicConnectionChannel(bdAddr, connectionOptions);
	client.addConnectionChannel(cc);
	cc.on("buttonSingleOrDoubleClickOrHold", function(clickType, wasQueued, timeDiff) {
		console.log(bdAddr + " " + clickType + " " + (wasQueued ? "wasQueued" : "notQueued") + " " + timeDiff + " seconds ago");

		var flicSelected = flicList.find(x => x.bAddr === bdAddr);

		if (flicSelected !== undefined && timeDiff <= maxQueuedTimeSecs) {
			if (connectionType == 'REST') {
				var options = {
					hostname: 'graph.api.smartthings.com',
					port: 443,
					method: 'PUT',
					//path: '/api/smartapps/installations/9f30551e-4cd0-4712-bd29-94d6c8c5b386/button/' + flicSelected.color + '/' + flicSelected.colorSeq + '/' + clickTypeMap.find(x => x.clickTypeIn === clickType).clickTypeOut + '?access_token=78fa363d-8f20-4d30-b524-1ee5a7a5f837',
					path: '/api/smartapps/installations/' + installationID + '/button/' + flicSelected.color + '/' + flicSelected.colorSeq + '/' + clickTypeMap.find(x => x.clickTypeIn === clickType).clickTypeOut + '?access_token=' + accessToken,
					header: 'Content-Type: application/x-www-form-urlencoded'
				};
	
				https.request(options, (res) => {
					res.on('error', function(e) {
						console.error('problem ' + e.message);
					});
				}).end(); 
			}
			else {
			// MQTT Connection
			mqttClient.publish('smartthings/Flic - ' + capitalizeFirstLetter(flicSelected.color) + ' ' + flicSelected.colorSeq + '/button/cmd', clickTypeMap.find(x => x.clickTypeIn === clickType).clickTypeOut); 
			console.log('mqtt: ' + 'smartthings/Flic - ' + capitalizeFirstLetter(flicSelected.color) + ' ' + flicSelected.colorSeq + '/button/cmd');
			}

		}
	});
}

client.once("ready", function() {
	console.log("Connected to daemon!");
	client.getInfo(function(info) {
		info.bdAddrOfVerifiedButtons.forEach(function(bdAddr) {
			listenToButton(bdAddr);
			var logText = bdAddr;
			var f = flicList.find(x => x.bAddr === bdAddr);
			if (f !== undefined) {
				logText += ' (' + f.color + ' ' + f.colorSeq + ')';
			}
			console.log(logText + ' : listener active');

		});
	});
});

client.on("bluetoothControllerStateChange", function(state) {
	console.log("Bluetooth controller state change: " + state);
});

client.on("newVerifiedButton", function(bdAddr) {
	console.log("A new button was added: " + bdAddr);
	listenToButton(bdAddr);
}); 

client.on("error", function(error) {
	console.error("Daemon connection error: " + error);
});

client.on("close", function(hadError) {
	console.error("Connection to daemon is now closed:" + hadError);
});

function capitalizeFirstLetter(string) {
    return string[0].toUpperCase() + string.slice(1);
}

function moscaServerStarted () {
	console.log('Mosca server is up and running');
}

