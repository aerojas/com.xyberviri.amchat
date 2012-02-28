package com.xyberviri.amchat;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;

public class AMChatRouter {
	AMChat amcMain;
			
	public AMChatRouter(AMChat amChat){
		this.amcMain = amChat;
	}	
	
	public void AMChatEvent(PlayerChatEvent event) {
		Player sender = event.getPlayer();
		String message = event.getMessage();
		amcMain.amcLogger.info(sender.getDisplayName()+": "+message);		
		// If were dealing with radio chat its going here.
		if(amcMain.isRadioOn(sender)&&amcMain.getPlayerMic(sender)){
			message = amcMain.amcTools.createMessage(sender, message);
			if (amcMain.getPlayerLinkID(sender).equalsIgnoreCase("none")){
				toRadio(sender,message);
				} 
			else if (amcMain.amcRadMan.isLinkValid(amcMain.getPlayerLinkID(sender))){
					amcMain.amcRadMan.linkMessage(amcMain.getPlayerLinkID(sender),message);
				} 
			else{
					amcMain.setPlayerLinkID(sender, "none");
					toRadio(sender,message);
				}			
			} 		
		else { // Not going over the radio deal with local
			toLocal(sender,(sender.getDisplayName()+": "+message));
			}
		
	
	}		
	
	public void toLocal(Player sender,String message){
		Location origin = sender.getLocation();
		for(Player player : Bukkit.getOnlinePlayers()){
			if	((player.hasPermission("amchat.local.hearall")||player.isOp())||(amcMain.limitChat() && (amcMain.amcTools.getDistance(origin,player.getLocation()) < amcMain.getMaxChat()))){
				player.sendMessage(message);
			} 
			else if (!amcMain.limitChat()){
				player.sendMessage(message);
			}
		}
			
	}
	
	public void toRadio(Player sender,String message){
		for(Player player : Bukkit.getOnlinePlayers()){
			if (amcMain.canReceive(sender,player)){
				if(amcMain.canRead(sender, player)){
					player.sendMessage(message);
					} 
				else {
					player.sendMessage(amcMain.amcTools.createBadMessage(message));
					}
			}
		}		
	}	
	
	
	// return true if we were successfully loaded
	public boolean isLoaded(AMChat amcMainPlugin) {
		if (this.amcMain.equals(amcMainPlugin)){
			return true;
			}
		return false;
	}
	
	
}//EOF
