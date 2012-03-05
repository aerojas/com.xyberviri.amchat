package com.xyberviri.amchat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.xyberviri.util.XYCustomConfig;



public class AMChatRadioManager {
	AMChat amcMain;
	
	private ArrayList<AMChatRadio> 		amRadioList;		//AMChat Radio's
	private Map <String, AMChatRadio> 	amRadioHandles;		//Handles to Radios
	private Map <Player, Boolean> 		amIsBuilding;		//Is the player building a radio?
		
	
	//private Map <Player, Integer> 		amRadioStep;		//What step is the player at in construction of a tower. 
	//this.amRadioStep = new HashMap<Player,Integer>();
	private XYCustomConfig radioConfig;
	
	
	
	AMChatRadioManager(AMChat amchat){
		this.amcMain = amchat;
		this.amRadioList = new ArrayList<AMChatRadio>();
		this.amRadioHandles = new HashMap<String, AMChatRadio>();
		this.amIsBuilding = new HashMap<Player,Boolean>();
		this.radioConfig = new XYCustomConfig(amcMain,"rm.settings.yml");
		loadRadioSettings();
			

	}
	
	public void amcRadManComCheck(){
		if (!amRadioList.isEmpty()){
			for(AMChatRadio amcRadio: amRadioList){
				amcRadio.chkValid();
			}
		}
	}
	
	//Save all the Radios to a file
	public void saveRadioSettings(){
		if(amRadioList.isEmpty()){
			amcMain.logMessage("note:no radios have been created, no save file will be created.");
		} else {
			amcMain.logMessage("Saving radio tower settings.");
		Map<String, Object> radManSettings = new HashMap<String,Object>();
		for(AMChatRadio varRadio :  amRadioList){		
			amcMain.logMessage(">"+varRadio.getName());
			radManSettings.put(varRadio.getName(), varRadio.getSettings());
		}
		this.radioConfig.get().createSection("radio-settings",radManSettings);
		this.radioConfig.saveToDisk();
		amcMain.logMessage(amRadioList.size()+" radio settings have been saved to file.");	
		}
	}	
	
	//Load all the Radios to the server from the settings file.
	public void loadRadioSettings(){
		if(radioConfig.get().isConfigurationSection("radio-settings")){
			ConfigurationSection radioSettingsFile = radioConfig.get().getConfigurationSection("radio-settings");
			
			if(radioSettingsFile.getKeys(false).isEmpty()){
				return;
			} else{
				int x=0;
				amcMain.logMessage("Loading radios.....");
				Set<String> varSetName = radioSettingsFile.getKeys(false);
				for(String radioSettings : varSetName){	
					
					Map<String, Object> radioSetting = new HashMap<String,Object>();
					radioSetting = radioSettingsFile.getConfigurationSection(radioSettings).getValues(true);
					String varStatus="SUCCESS";
					
					if(createRadio(radioSetting)){						
						x++;
					} else {varStatus="FAILED";}
					amcMain.logMessage("Loading: "+radioSettings+" "+varStatus);
					
				}
				amcMain.logMessage("Complete "+x+" radios loaded");
			}
		} else {
			amcMain.logMessage("No rm.settings.yml file detected, has anyone built any towers?");
		}
	}
	
	//This creates a radio from a Object Map	
	@SuppressWarnings("unchecked")
	private boolean createRadio(Map<String,Object> radioSettings){
		boolean b=true;
		if(radioSettings.containsKey("radio-id")){
			AMChatRadio newRadio = new AMChatRadio(this);
			newRadio.setName((String) radioSettings.get("radio-id"));
			if(radioSettings.containsKey("owner")){
				newRadio.setOwner((String) radioSettings.get("owner"));}
			if(radioSettings.containsKey("freq")){
				newRadio.setChan((Integer) radioSettings.get("freq"));}
			if(radioSettings.containsKey("code")){
				newRadio.setCode((Integer) radioSettings.get("code"));}
			if(radioSettings.containsKey("pass")){
				newRadio.setPass((String) radioSettings.get("pass"));}
			if(radioSettings.containsKey("locw")
			 &&radioSettings.containsKey("locx")
			 &&radioSettings.containsKey("locy")
			 &&radioSettings.containsKey("locz")){
				double x,y,z;
				x=(Double) radioSettings.get("locx");
				y=(Double) radioSettings.get("locy");
				z=(Double) radioSettings.get("locz");
				Location varLoc = new Location(Bukkit.getServer().getWorld((String) radioSettings.get("locw")), x, y,z);
				newRadio.setLoc(varLoc);
			}
			
			//Arrays freak eclipse out when trying to read these back. 
			if(radioSettings.containsKey("admins")){
				Object objAdmin=radioSettings.get("admins");
				if (objAdmin instanceof ArrayList<?>){
				newRadio.setAdmins((ArrayList<String>) objAdmin);	
				}				
			}
			if(radioSettings.containsKey("members")){
				Object objMembers=radioSettings.get("members");
				if (objMembers instanceof ArrayList<?>){
				newRadio.setMembers((ArrayList<String>) objMembers);	
				}	
			}
			if(radioSettings.containsKey("radio-isadmin")){
				newRadio.setAdmin((Boolean) radioSettings.get("radio-isadmin"));}	
			//If ever thing was loaded correctly then we add it to the list of radios
			//Also note by not putting it in this list it wont be saved. 
			if(b){
			this.amRadioList.add(newRadio);
			this.amRadioHandles.put((String) radioSettings.get("radio-id"), newRadio);
			}
		} 
		else{b=false;}
	
		return b;		
	}
	


	//Creates a new radio, this is from a player perspective. 
	public void createNewRadio(Player player,Location radioLocation){
		AMChatRadio newRadio = new AMChatRadio(this);
		String varNewRadioHandle = genRadioID(false);
		newRadio.setName(varNewRadioHandle);
		newRadio.setOwner(player.getDisplayName());
		newRadio.setLoc(radioLocation);
		newRadio.setChan(amcMain.getPlayerRadioChannel(player));
		newRadio.setCode(amcMain.getPlayerRadioCode(player));
		newRadio.setPass(""+varNewRadioHandle.hashCode());		
		newRadio.addMember(player.getDisplayName());
		newRadio.addAdmin(player.getDisplayName());
		amRadioList.add(newRadio);
		amRadioHandles.put(varNewRadioHandle, newRadio);
		amcMain.setPlayerLinkID(player, varNewRadioHandle);
		amcMain.amcTools.msgToPlayer(player, " created a new radio with id:",varNewRadioHandle);
		amcMain.logMessage(player.getDisplayName()+" created a new radio with id:"+varNewRadioHandle);
	}
	

	
	public boolean isPlayerBuilding(Player thisPlayer){
		if(amIsBuilding.containsKey(thisPlayer)){
			return amIsBuilding.get(thisPlayer);
		} else {
			this.amIsBuilding.put(thisPlayer, false);
			return false;
		}
	}
	
	public boolean isLoaded(AMChat amcMainPlugin) {
		if (this.amcMain.equals(amcMainPlugin)){			
			return true;
			}
		return false;
	}

	
	public String genRadioID(boolean useAlt){
		//Serial numbers are <HOUR><DAY><#> with the number being the number of radios with that id already.
		String newRadioIDPrefix;
		String newRadioIDString;
		int varOffset=1;
		
		if(useAlt){			
			newRadioIDPrefix="ZX";
			} 
		else{
			String[] varHour = {"Z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y"};
			String[] varDay = {"X","S","M","T","W","H","F","A","Z"};			
			Calendar this_moment = Calendar.getInstance(); 
			newRadioIDPrefix = varHour[this_moment.get(Calendar.HOUR_OF_DAY)]+varDay[this_moment.get(Calendar.DAY_OF_WEEK)];
		}

		while(true){			
			if(varOffset>9){ 	//If the id for this prefix is 10+ use that to construct the string.
				newRadioIDString=newRadioIDPrefix+varOffset;} 
			else{ 				//pad 0-9 with leading zero so we have at least 4 digits for id's.
				newRadioIDString=newRadioIDPrefix+String.format("%02d",varOffset);
				}
			
			if (!isLinkValid(newRadioIDString)){ //if not in use break.
				break;
				}
			varOffset++;
		}
		return newRadioIDString;
	}	
	
	public boolean isLinkValid(String radioID) {
	 return amRadioHandles.containsKey(radioID);
	}

	public void linkMessage(String playerLinkID, String message) {
		// TODO Auto-generated method stub
		
	}
	
	//This creates the Object Map from a Radio
//	private Map<String, Object> createRadioSettings(AMChatRadio amcRadio){
//		Map<String, Object> radioSettings = new HashMap<String,Object>();
//		Map<String, Object> playerSetting = new HashMap<String,Object>();
//		playerSetting.put("radio",isRadioOn(player));
//		playerSetting.put("freq",getPlayerRadioChannel(player));	
//		playerSetting.put("code",getPlayerRadioCode(player));
//		playerSetting.put("mic",getPlayerMic(player));
//		playerSetting.put("filter",getPlayerFilter(player));
//		playerSetting.put("cutoff",getPlayerCutoff(player));
//		playerSetting.put("link",getPlayerLinkID(player));
//		//playerSettings.put(player.getDisplayName(), playerSetting);
//		//playerRadioConfig.createSection("radio-settings",playerSettings);
//		playerRadioConfig.createSection(player.getDisplayName(), playerSetting);
//		this.saveConfigPlayerRadioSettings();
//		return radioSettings;
//	}
//	public void amcRadManFileTest(String varString){
//	amcMain.logMessage("AMCRadManFileTest: "+varString);
//	Map<String, Object> radioSettings = new HashMap<String,Object>();
//	radioSettings.put("name", varString);
//	radioSettings.put("owner", "Xyberviri");
//	radioSettings.put("freq", "100");
//	radioSettings.put("code", "0");
//	radioSettings.put("pass", "none");
//	radioSettings.put("locw", "nether");
//	radioSettings.put("locx", "0");
//	radioSettings.put("locy", "64");
//	radioSettings.put("locz", "0");
//	ArrayList<String> radioMembers = new ArrayList<String>();
//	ArrayList<String> radioAdmins = new ArrayList<String>();
//	radioAdmins.add("Xyberviri");
//	radioMembers.add("Xyberviri");
//	radioMembers.add("rumblegoat");
//	radioSettings.put("admins", radioAdmins);
//	radioSettings.put("members", radioMembers);
//	radioConfig.get().createSection(varString, radioSettings);
//	radioConfig.saveToDisk();
//
//}	
//	public void amcRadManSerializationTest(String varString){
//		AMChatRadio newRadio = new AMChatRadio(this);
//		String varNewRadioHandle = genRadioID(false);
//
//		newRadio.setName(varNewRadioHandle);
//		newRadio.setOwner(varString+" owner");
//		//newRadio.setLoc("world",0.0,0.0,0.0);
//		newRadio.setChan(1);
//		newRadio.setCode(0);
//		newRadio.setPass("pass");		
//		newRadio.addMember("Member1");
//		newRadio.addMember("Member2");
//		newRadio.addAdmin("Member1");
//		
//
//		amRadioList.add(newRadio);
//		amRadioHandles.put(varNewRadioHandle, newRadio);
//	}
		
}