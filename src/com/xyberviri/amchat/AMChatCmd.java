package com.xyberviri.amchat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class AMChatCmd implements CommandExecutor {
	AMChat amcMain;
	
	AMChatCmd(AMChat amcMain){
		this.amcMain = amcMain;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,String[] args) {
		if (!(sender instanceof Player)) {
			if (cmd.getName().equalsIgnoreCase("am") && args.length == 0){
				amcMain.logMessage("your mighty radio hears and is heard by all, there are no settings to change.");
				return true;
			}
			
			if (cmd.getName().equalsIgnoreCase("am") && args[0].equalsIgnoreCase("list")){
				if (amcMain.getRadioPlayers().size() > 0){
						amcMain.logMessage("There are "+amcMain.getRadioPlayers().size()+" players online with active radio");
						for (String playerName : amcMain.getRadioPlayers()){
							Player player = Bukkit.getServer().getPlayer(playerName);
					    	if (player != null){
					    	amcMain.logMessage("["+playerName+"] fq:"+amcMain.getPlayerRadioChannel(player)+", cd:"+amcMain.getPlayerRadioCode(player)+", mic open:"+amcMain.getPlayerMic(player)+", filter on:"+amcMain.getPlayerFilter(player)+", cutoff:"+amcMain.getPlayerCutoff(player));
					    	}	
						}
					} 
				else {amcMain.logMessage("There are no players online with active radios");
					}
				return true;
			}
			amcMain.logMessage("That is not avalible from the console");
			return true;
		}	
		
		Player player = (Player) sender;

		//AM command branch
		if (cmd.getName().equalsIgnoreCase("am")){
			
			
		//am
		if (player.hasPermission("amchat.radio.personal.use") && args.length == 0){
			amcMain.amcTools.msgToPlayer(player,"[-Xmit-Freq-]:"," "+amcMain.getPlayerRadioChannel(player));
			amcMain.amcTools.msgToPlayer(player,"[-Xmit-Code-]:"," "+amcMain.getPlayerRadioCode(player));
			amcMain.amcTools.msgToPlayer(player,"[-Xmit-Link-]:"," "+amcMain.getPlayerLinkID(player));
			amcMain.amcTools.msgToPlayer(player,"[--Cut-Off--]:"," "+amcMain.getPlayerCutoff(player));
			amcMain.amcTools.msgToPlayer(player,"[--Mic-Open-]:"," "+amcMain.getPlayerMic(player));
			amcMain.amcTools.msgToPlayer(player,"[--Filter---]:"," "+amcMain.getPlayerFilter(player));
			return true;
		}
		
		//am radio
		if(player.hasPermission("amchat.radio.personal.radio") && args[0].equalsIgnoreCase("radio") && args.length == 1){
			amcMain.togglePlayerRadio(player);
			return true;
		}
		
		//am mic
		if(player.hasPermission("amchat.radio.personal.mic") && args[0].equalsIgnoreCase("mic") && args.length == 1){
			amcMain.togglePlayerMic(player);
			return true;
		}
		
		//am filter
		if(player.hasPermission("amchat.radio.personal.filter") && args[0].equalsIgnoreCase("filter") && args.length == 1){
			amcMain.togglePlayerFilter(player);
			return true;
		}
		
		//am tune <#>
		if(player.hasPermission("amchat.radio.personal.tune") && args[0].equalsIgnoreCase("tune") && args.length == 2){
			try{ 
				Integer targetValue = Integer.parseInt(args[1]);
				
				if((targetValue<amcMain.varRadioMinFreq || targetValue>amcMain.varRadioMaxFreq)&&(!player.hasPermission("amchat.radio.override.tune"))&&(!player.isOp())){
					amcMain.amcTools.errorToPlayer(player,"Valid Frequencies are "+amcMain.varRadioMinFreq+"-"+amcMain.varRadioMaxFreq);
					return true;
				}				
				amcMain.tunePlayerRadioChannel(player, targetValue);
				return true;
			} catch (NumberFormatException e){
				amcMain.amcTools.errorToPlayer(player,args[1] + "is not a number!");
				return true;
				}
		}		
		
		//am code <#>
		if(player.hasPermission("amchat.radio.personal.code") && args[0].equalsIgnoreCase("code") && args.length == 2){
			try{ 
				Integer targetValue = Integer.parseInt(args[1]);
				
				if ((targetValue<0)&&(!player.hasPermission("amchat.radio.override.code")))
				{targetValue=0;}
				
				if ((targetValue>amcMain.varRadioMaxCode)&&(!player.hasPermission("amchat.radio.override.code"))){
					amcMain.amcTools.errorToPlayer(player,"Valid Code is 0-"+amcMain.varRadioMaxCode);
					return true;					
				}
				
 
				amcMain.setPlayerRadioCode(player, targetValue);
				return true;
			} catch (NumberFormatException e){
				amcMain.amcTools.errorToPlayer(player,args[1] + "is not a number!");
				return true;
				}
		}		

		//am cutoff <#>
		if(player.hasPermission("amchat.radio.personal.cutoff") && args[0].equalsIgnoreCase("cutoff") && args.length == 2){
			try{ 
				Integer targetValue = Integer.parseInt(args[1]);
				if((targetValue>amcMain.varRadioMaxCuttoff)&&(!player.hasPermission("amchat.radio.override.cutoff"))&&(!player.isOp())){
					amcMain.amcTools.errorToPlayer(player,"Valid Cutoff is 0-"+amcMain.varRadioMaxCuttoff);
					return true;
				} else if (targetValue<0){
					amcMain.amcTools.errorToPlayer(player,"Valid Cutoff is 0-"+amcMain.varRadioMaxCuttoff);
					targetValue=0;
					}
				amcMain.setPlayerRadioCutoff(player, targetValue);
				return true;
			} catch (NumberFormatException e){
				amcMain.amcTools.errorToPlayer(player,args[1] + "is not a number!");
				return true;
				}
		}		
		
		//am home
		if(player.hasPermission("amchat.radio.personal.home") && args[0].equalsIgnoreCase("home") && args.length == 1){
			if(!amcMain.isRadioOn(player)){
				amcMain.togglePlayerRadio(player);
			}
			if(!amcMain.getPlayerMic(player)){
				amcMain.togglePlayerMic(player);	
			}
			if(amcMain.getPlayerFilter(player)){
				amcMain.togglePlayerFilter(player);
			}			
			amcMain.tunePlayerRadioChannel(player, amcMain.varRadioDefFreq);
			amcMain.setPlayerRadioCode(player, 0);
			amcMain.setPlayerRadioCutoff(player, amcMain.varRadioMaxCuttoff);
			return true;
		}			
		
		//am ping
		if(player.hasPermission("amchat.radio.personal.ping") && args[0].equalsIgnoreCase("ping") && args.length == 2){
			Player other = Bukkit.getServer().getPlayer(args[1]);
	        if (other == null) {
	        	amcMain.amcTools.errorToPlayer(player, args[1] + " is not online!");
	        	return true;
	        }
			amcMain.playerRadioPing(player,other);
			return true;
		}
		
		return false;
		}
		//Else if  were using XM commands 
		else if (cmd.getName().equalsIgnoreCase("xm")){
			
			
			
			return false;
		}
		
		
		
		
		
		return false;
	}

	
	
	
	
	
	// return true if we were successfully loaded
	public boolean isLoaded(AMChat amcMainPlugin) {
		if (this.amcMain.equals(amcMainPlugin)){
			return true;
			}
		return false;
	}
	
}
