package com.aeron.aeronjmx;

import static io.aeron.driver.status.PublisherLimit.PUBLISHER_LIMIT_TYPE_ID;
import static io.aeron.driver.status.ReceiveChannelStatus.RECEIVE_CHANNEL_STATUS_TYPE_ID;
import static io.aeron.driver.status.ReceiverPos.RECEIVER_POS_TYPE_ID;
import static io.aeron.driver.status.SendChannelStatus.SEND_CHANNEL_STATUS_TYPE_ID;
import static io.aeron.driver.status.StreamPositionCounter.CHANNEL_OFFSET;
import static io.aeron.driver.status.StreamPositionCounter.REGISTRATION_ID_OFFSET;
import static io.aeron.driver.status.StreamPositionCounter.SESSION_ID_OFFSET;
import static io.aeron.driver.status.StreamPositionCounter.STREAM_ID_OFFSET;
import static io.aeron.driver.status.SystemCounterDescriptor.SYSTEM_COUNTER_TYPE_ID;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

import org.agrona.DirectBuffer;
import org.apache.commons.lang3.text.WordUtils;

import io.aeron.driver.status.ChannelEndpointStatus;

public class CounterInfo {
	private String name;
	private long value;
	
	public static CounterInfo get(String label, DirectBuffer keyBuffer, int typeId, long value) {
		if (typeId == SYSTEM_COUNTER_TYPE_ID || typeId == PUBLISHER_LIMIT_TYPE_ID || typeId == RECEIVER_POS_TYPE_ID || typeId == SEND_CHANNEL_STATUS_TYPE_ID || typeId == RECEIVE_CHANNEL_STATUS_TYPE_ID)
			return new CounterInfo(label, keyBuffer, typeId, value);
			
		return null;
	}
	
	private CounterInfo(String label, DirectBuffer keyBuffer, int typeId, long value) {		
		this.value = value;
		
		if (typeId == SYSTEM_COUNTER_TYPE_ID) {
			setSystemCounter(label, keyBuffer);

		} else if (typeId == PUBLISHER_LIMIT_TYPE_ID || typeId == RECEIVER_POS_TYPE_ID) {
			setStreamCounterWithChannel(label, keyBuffer);
		} 
		else if (typeId == SEND_CHANNEL_STATUS_TYPE_ID || typeId == RECEIVE_CHANNEL_STATUS_TYPE_ID) {			
			setStreamCounterWithChannelStatus(label, keyBuffer);
		}		
	}
	
	public CounterInfo(String name, long value) {
		super();
		this.name = name;
		this.value = value;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public long getValue() {
		return value;
	}


	public void setValue(long value) {
		this.value = value;
	}


	@Override
	public String toString() {
		return "CounterInfo [name=" + name + ", value=" + value + "]";
	}
	
	
	private void setStreamCounter(String label, DirectBuffer keyBuffer, String channel) {
		int registrationId = keyBuffer.getInt(REGISTRATION_ID_OFFSET);
		String type = getType(label);
		long sessionId = keyBuffer.getLong(SESSION_ID_OFFSET);
		long streamId = keyBuffer.getLong(STREAM_ID_OFFSET);
		channel = channel.replaceAll("[:\\?\\=]", "_");
		name = "com.jmxaeron:type=StreamCounter,subType=" + type + ",registrationId="+registrationId+",sessionId=" + sessionId + ",streamId=" + streamId + ",channel="+channel;
	}
	
	private void setStreamCounterWithChannel(String label, DirectBuffer keyBuffer) {		
		String channel = keyBuffer.getStringUtf8(CHANNEL_OFFSET);
		setStreamCounter(label, keyBuffer, channel);
	}
	
	private void setStreamCounterWithChannelStatus(String label, DirectBuffer keyBuffer) {		
		String channel = keyBuffer.getStringUtf8(ChannelEndpointStatus.CHANNEL_OFFSET);
		setStreamCounter(label, keyBuffer, channel);
	}
	
	private void setSystemCounter(String label, DirectBuffer keyBuffer) {
		int registrationId = keyBuffer.getInt(REGISTRATION_ID_OFFSET);
		String type = getType(label);
		name = "com.jmxaeron:type=SystemCounter,subType=" + type + ",registrationId="+registrationId;
	}
	

	public MBeanInfo getMBeanInfo() {
		List<MBeanAttributeInfo> attInfoList = new ArrayList<MBeanAttributeInfo>();
		attInfoList.add(new MBeanAttributeInfo("value", Long.class.getName(), "", true, false, false));
		return new MBeanInfo(getName(), "", attInfoList.toArray(new MBeanAttributeInfo[] {}), null, null, null);
	}
	
	private String getType(String label) {
		String type = label.split(":")[0];
		type = WordUtils.capitalizeFully(type).replaceAll(" ", "");
		return type;
	}
	
	
	
	
	
	
	
}
