package com.aeron.aeronjmx;

import static io.aeron.CncFileDescriptor.cncVersionOffset;
import static io.aeron.CncFileDescriptor.createCountersMetaDataBuffer;
import static io.aeron.CncFileDescriptor.createCountersValuesBuffer;
import static io.aeron.CncFileDescriptor.createMetaDataBuffer;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

import org.agrona.DirectBuffer;
import org.agrona.IoUtil;
import org.agrona.concurrent.status.CountersReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.aeron.CncFileDescriptor;
import io.aeron.CommonContext;

public class JMXCountersReader {	
	
	private static final Logger LOG = LogManager.getLogger(JMXCountersReader.class);
	private CountersReader countersReader;
	
	public JMXCountersReader() {
		final File aeronCncFile = CommonContext.newDefaultCncFile();
		setup(aeronCncFile);
	}
	
	public JMXCountersReader(String aeronCncFileStr) {
		final File aeronCncFile = new File(aeronCncFileStr);
		setup(aeronCncFile);
	}
	
	private void setup(File aeronCncFile) {
		MappedByteBuffer cncByteBuffer = IoUtil.mapExistingFile(aeronCncFile, "cnc");
		DirectBuffer cncMetaData = createMetaDataBuffer(cncByteBuffer);
		int cncVersion = cncMetaData.getInt(cncVersionOffset(0));

		if (CncFileDescriptor.CNC_VERSION != cncVersion) {
			throw new IllegalStateException("CnC version not supported: file version=" + cncVersion);
		}

		countersReader = new CountersReader(createCountersMetaDataBuffer(cncByteBuffer, cncMetaData), createCountersValuesBuffer(cncByteBuffer, cncMetaData));
	}
	
	public List<CounterInfo> read() {		

		List<CounterInfo> counters = new ArrayList<CounterInfo>();

		countersReader.forEach((counterId, typeId, keyBuffer, label) -> {
			
			final long value = countersReader.getCounterValue(counterId);
			CounterInfo counterInfo = CounterInfo.get(label, keyBuffer, typeId, value);		
			
			if (counterInfo != null)
				counters.add(counterInfo);
			
			LOG.debug(String.format("%3d: %,20d - %s", counterId, value, label));

		});		
		
		return counters;
	}
	
	public List<MBeanInfo> buildDynamicMBeanInfos() {
		List<MBeanInfo> list = new ArrayList<>();		

		List<CounterInfo> counters = read();
		
		for (CounterInfo counterInfo : counters) {
			List<MBeanAttributeInfo> attInfoList = new ArrayList<MBeanAttributeInfo>();
			attInfoList.add(new MBeanAttributeInfo("value", Long.class.getName(), "", true, false, false));
			list.add(new MBeanInfo(counterInfo.getName(), "", attInfoList.toArray(new MBeanAttributeInfo[] {}), null, null, null));
					
		}		
		return list;
	}
}
