package net.suntec.sdl.rpc;

import java.util.HashMap;
import java.util.Map;

import com.smartdevicelink.proxy.rpc.AddCommand;
import com.smartdevicelink.proxy.rpc.AddSubMenu;
import com.smartdevicelink.proxy.rpc.Alert;
import com.smartdevicelink.proxy.rpc.AlertManeuver;
import com.smartdevicelink.proxy.rpc.ChangeRegistration;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSet;
import com.smartdevicelink.proxy.rpc.DeleteCommand;
import com.smartdevicelink.proxy.rpc.DeleteFile;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSet;
import com.smartdevicelink.proxy.rpc.DeleteSubMenu;
import com.smartdevicelink.proxy.rpc.DiagnosticMessage;
import com.smartdevicelink.proxy.rpc.DialNumber;
import com.smartdevicelink.proxy.rpc.EndAudioPassThru;
import com.smartdevicelink.proxy.rpc.GetDTCs;
import com.smartdevicelink.proxy.rpc.GetVehicleData;
import com.smartdevicelink.proxy.rpc.ListFiles;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThru;
import com.smartdevicelink.proxy.rpc.PerformInteraction;
import com.smartdevicelink.proxy.rpc.PutFile;
import com.smartdevicelink.proxy.rpc.ReadDID;
import com.smartdevicelink.proxy.rpc.ResetGlobalProperties;
import com.smartdevicelink.proxy.rpc.ScrollableMessage;
import com.smartdevicelink.proxy.rpc.SendLocation;
import com.smartdevicelink.proxy.rpc.SetAppIcon;
import com.smartdevicelink.proxy.rpc.SetDisplayLayout;
import com.smartdevicelink.proxy.rpc.SetGlobalProperties;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimer;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.ShowConstantTbt;
import com.smartdevicelink.proxy.rpc.Slider;
import com.smartdevicelink.proxy.rpc.Speak;
import com.smartdevicelink.proxy.rpc.SubscribeButton;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleData;
import com.smartdevicelink.proxy.rpc.SystemRequest;
import com.smartdevicelink.proxy.rpc.UnregisterAppInterface;
import com.smartdevicelink.proxy.rpc.UnsubscribeButton;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleData;
import com.smartdevicelink.proxy.rpc.UpdateTurnList;

/**
 * 
 * @author sangjun
 * @mail   yeahsj@gmail.com
 */
public class RpcRequestMapper {
	static Map<String, Class<?>> mappers = new HashMap<String, Class<?>>();

	static {
		mappers.put("AddCommand", AddCommand.class);
		mappers.put("AddSubMenu", AddSubMenu.class);
		mappers.put("Alert", Alert.class);
		mappers.put("AlertManeuver", AlertManeuver.class);
		mappers.put("ChangeRegistration", ChangeRegistration.class);
		mappers.put("CreateInteractionChoiceSet",
				CreateInteractionChoiceSet.class);
		mappers.put("DeleteCommand", DeleteCommand.class);
		mappers.put("DeleteFile", DeleteFile.class);
		mappers.put("DeleteInteractionChoiceSet",
				DeleteInteractionChoiceSet.class);
		mappers.put("DeleteSubMenu", DeleteSubMenu.class);
		mappers.put("DiagnosticMessage", DiagnosticMessage.class);
		mappers.put("DialNumber", DialNumber.class);
		mappers.put("EndAudioPassThru", EndAudioPassThru.class);
		mappers.put("GetDTCs", GetDTCs.class);
		mappers.put("GetVehicleData", GetVehicleData.class);
		mappers.put("ListFiles", ListFiles.class);
		mappers.put("PerformAudioPassThru", PerformAudioPassThru.class);
		mappers.put("PerformInteraction", PerformInteraction.class);
		mappers.put("PutFile", PutFile.class);
		mappers.put("ReadDID", ReadDID.class);
		mappers.put("ResetGlobalProperties", ResetGlobalProperties.class);
		mappers.put("ScrollableMessage", ScrollableMessage.class);
		mappers.put("SendLocation", SendLocation.class);
		mappers.put("SetAppIcon", SetAppIcon.class);
		mappers.put("SetDisplayLayout", SetDisplayLayout.class);
		mappers.put("SetGlobalProperties", SetGlobalProperties.class);
		mappers.put("SetMediaClockTimer", SetMediaClockTimer.class);
		mappers.put("Show", Show.class);
		mappers.put("ShowConstantTbt", ShowConstantTbt.class);
		mappers.put("Slider", Slider.class);
		mappers.put("Speak", Speak.class);
		mappers.put("SubscribeButton", SubscribeButton.class);
		mappers.put("SubscribeVehicleData", SubscribeVehicleData.class);
		mappers.put("SystemRequest", SystemRequest.class);
		mappers.put("UnregisterAppInterface", UnregisterAppInterface.class);
		mappers.put("UnsubscribeButton", UnsubscribeButton.class);
		mappers.put("UnsubscribeVehicleData", UnsubscribeVehicleData.class);
		mappers.put("UpdateTurnList", UpdateTurnList.class);
	}

	public static Class<?> get(String key) {
		return mappers.get(key);
	}
}
