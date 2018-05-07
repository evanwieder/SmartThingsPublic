/**
 *
 *  Device handler used for the flic button.
 *  MODIFIED BY evanwieder
 *  
 *  The mapping between action and button numbers are as follow, this will be very usefl if using CoRE smartApps:
 *    Click event:         Button#1 -> pushed
 *    Double Click event:  Button#2 -> pushed
 *    Hold event           Button#3 -> pushed
 * 
 *  Copyright 2016 jbisson
 *
 *
 *  Revision History
 *  ==============================================
 *  2018                      Fixed spellings and app settings
 *  2016-11-22 Version 1.1.0  Changed the button to use 3 internal button allowing mapping between an action to a button number. 
 *                            (Button#1 -> Click, Button#2 -> doubleClick, Button#3 -> Hold).
 *  2016-08-21 Version 1.0.0  Initial commit
 *  
 *	2018 - VERSION 1..
 */
 
def clientVersion() {
    return "1.1.0 - 2016-11-22"
}

metadata {
	definition (name: "Flic Button", namespace: "jbisson", author: "Jonathan Bisson") {	
		capability "Button"
        capability "Refresh"
        
		command "click"
		command "doubleClick"
		command "hold"
        
        command "clearSingleClickStatus"
        command "clearDoubleClickStatus"
        command "clearHoldStatus"        
        
        attribute "flicColor", "String"
        attribute "buttonNumber", "String"
	}

	tiles(scale: 2) {
        multiAttributeTile(name:"button", type:"generic", width:6, height:4) {
            tileAttribute("flicColor", key: "PRIMARY_CONTROL") {
                attributeState "white", label:'', backgroundColor: "#FFFFFF"
                attributeState "black", label:'', backgroundColor: "#000000"
                attributeState "green", label:'', backgroundColor: "#89BF47"
                attributeState "yellow", label:'', backgroundColor: "#DDD685"
                attributeState "turquoise", label:'', backgroundColor: "#46A09E"
            }
            
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
                attributeState "default", label:'${currentValue}'
            }
    	}
        
         standardTile("icon", "icon") {         
			state "white", label:'', backgroundColor: "#FFFFFF", action: "dummy", canChangeIcon: true, icon: "st.Lighting.light21"
            state "black", label:'', backgroundColor: "#000000", action: "dummy", canChangeIcon: true, icon: "st.Lighting.light21"
            state "green", label:'', backgroundColor: "#89BF47", action: "dummy", canChangeIcon: true, icon: "st.Lighting.light21"
            state "yellow", label:'', backgroundColor: "#DDD685", action: "dummy", canChangeIcon: true, icon: "st.Lighting.light21"
            state "turquoise", label:'', backgroundColor: "#46A09E", action: "dummy", canChangeIcon: true, icon: "st.Lighting.light21"
         }
        
        standardTile("singleClick", "singleClick", width: 2, height: 2) {
            state "default", label:"Click", action:"click", canChangeIcon: true, icon:"st.Lighting.light21", nextState:"withBackground"
            state "withBackground", label:"Click", canChangeIcon: true, icon:"st.Lighting.light21", backgroundColor: "#FF0000", nextState:"default"
        }
        
        standardTile("doubleClick", "doubleClick", width: 2, height: 2) {
            state "default", label:"2x Click", action:"doubleClick", canChangeIcon: true, icon:"st.Lighting.light21", nextState:"withBackground"
            state "withBackground", label:"2x Click", canChangeIcon: true, icon:"st.Lighting.light21",  backgroundColor: "#FF0000", nextState:"default"
        }
        
        standardTile("hold", "hold", width: 2, height: 2) {
            state "default", label:"Hold", action:"hold", canChangeIcon: true, icon:"st.Lighting.light21", nextState:"withBackground"
            state "withBackground", label:"Hold", canChangeIcon: true, icon:"st.Lighting.light21",  backgroundColor: "#FF0000", nextState:"default"                        
        }
        
        valueTile("singleClickStatus", "singleClickStatus", decoration: "flat",  inactiveLabel: false, width: 2, height: 1) {
            state "default", label:'Last click event \n ${currentValue}', action:"clearSingleClickStatus"
        }
        
        valueTile("doubleClickStatus", "doubleClickStatus", decoration: "flat",  inactiveLabel: false, width: 2, height: 1) {
            state "default", label:'Last double click event \n ${currentValue}', action:"clearDoubleClickStatus"
        }
        
        valueTile("holdStatus", "holdStatus", decoration: "flat", inactiveLabel: false, width: 2, height: 1) {
            state "default", label:'Last hold event\n ${currentValue}', action:"clearHoldStatus"
        }
        
		main(["icon"])
		details(["button", "singleClick", "doubleClick", "hold", "singleClickStatus", "doubleClickStatus", "holdStatus"])
	}
}   

preferences {
	input title: "Flic Button", description: "v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"
    input name: "isLogLevelTrace", type: "bool", title: "Show trace log level ?\n", defaultValue: "false"
    input name: "isLogLevelDebug", type: "bool", title: "Show debug log level ?\n", defaultValue: "true"
	
    input name: "colorEnum", type: "enum", title: "Color of the flic button\n", options: ["black", "white", "turquoise", "green", "yellow"], required: true
    input name: "buttonNumberPref", type: "decimal", title: "Identitfy the button number (if you have more than one with the same color) to differentiate them.\n", defaultValue: "0"
}

/*******************************************************************************
*	Methods                                                                    *
*******************************************************************************/

/**
 *  updated - Called when the preferences of the device type are changed
 */
def updated() {
	logDebug "updated() $colorEnum"    
    sendEvent(name: "flicColor", value: "$colorEnum", isStateChange: true)
    sendEvent(name: "icon", value: "$colorEnum", displayed: false, isStateChange: true)    
    sendEvent(name: "buttonNumber", value: "$buttonNumberPref", isStateChange: true)
    sendEvent(name: "numberOfButtons", value: "3", isStateChange: true)
    
    sendEvent(name: "status", value: "---", displayed: false)
    sendEvent(name: "singleClickStatus", displayed: false, value: "")
    sendEvent(name: "doubleClickStatus", displayed: false, value: "")
    sendEvent(name: "holdStatus", displayed: false, value: "")
}

def click() {
	logDebug "click() $colorEnum"	
    
    def currentDateTime = new Date().format('yyyy-M-d hh:mm:ss', location.timeZone)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName was clicked", isStateChange: true)        
    sendEvent(name: "status", value: "Last event: Click at ${currentDateTime}", displayed: false)
    sendEvent(name: "singleClickStatus", value: currentDateTime, displayed: false)
    
    runIn(1, clearBackground)
}

def doubleClick() {
	logDebug "doubleClick()"
    
	def currentDateTime = new Date().format('yyyy-M-d hh:mm:ss', location.timeZone)	
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 2], descriptionText: "$device.displayName was double clicked", isStateChange: true)    
    sendEvent(name: "status", value: "Last event: 2x Click at ${currentDateTime}", displayed: false)
    sendEvent(name: "doubleClickStatus", value: currentDateTime, displayed: false)
    
    runIn(1, clearBackgroundAndReflash)
}

def hold() {
	logDebug "hold()"
    
    def currentDateTime = new Date().format('yyyy-M-d hh:mm:ss', location.timeZone)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 3], descriptionText: "$device.displayName was held", isStateChange: true)    
    sendEvent(name: "status", value: "Last event: Hold at ${currentDateTime}", displayed: false)
    sendEvent(name: "holdStatus", value: currentDateTime, displayed: false)    
    
     runIn(2, clearBackground)
}

def clearBackgroundAndReflash() {
	clearBackground()
    
    runIn(1, flashDoubleClick)
}

def flashDoubleClick() {
	sendEvent(name: "doubleClick", value: "withBackground", displayed: false, isStateChange: true)
	runIn(1, clearBackground)
}

def clearBackground() {
	logTrace "clearBackground()"
    
    sendEvent(name: "singleClick", value: "default", displayed: false, isStateChange: true)
    sendEvent(name: "doubleClick", value: "default", displayed: false, isStateChange: true)
    sendEvent(name: "hold", value: "default", displayed: false, isStateChange: true)
}

def clearSingleClickStatus() {
	sendEvent(name: "singleClickStatus", displayed: false, value: "", isStateChange: true)
}

def clearDoubleClickStatus() {
	sendEvent(name: "doubleClickStatus", displayed: false, value: "", isStateChange: true)
}

def clearHoldStatus() {
	sendEvent(name: "holdStatus", displayed: false, value: "", isStateChange: true)
}

void logDebug(str) {	
	if (isLogLevelDebug) {    	
        log.debug str
	}
}

void logTrace(str) {
	if (isLogLevelTrace) {
        log.trace str 
	}
}