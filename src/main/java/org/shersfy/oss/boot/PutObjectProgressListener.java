package org.shersfy.oss.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.gouuse.datahub.commons.utils.FileUtil.FileSizeUnit;

public class PutObjectProgressListener implements ProgressListener {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(PutObjectProgressListener.class);

	private long bytesWritten = 0;
	private long totalBytes = -1;
	private boolean succeed = false;
	private String filename = "";
	
	public PutObjectProgressListener(String filename){
		this.filename = filename;	
	}

	@Override
	public void progressChanged(ProgressEvent progressEvent) {
		long bytes = progressEvent.getBytes();
		ProgressEventType eventType = progressEvent.getEventType();
		String info = "";
		FileSizeUnit unit;
		switch (eventType) {
		case TRANSFER_STARTED_EVENT:
			LOGGER.info("==================================================");
			LOGGER.info("Start to upload {} ...", filename);
			break;
		case REQUEST_CONTENT_LENGTH_EVENT:
			this.totalBytes = bytes;
			unit = FileSizeUnit.countUnit(this.totalBytes);
			info = "%.3f%s in total will be uploaded to OSS";
			LOGGER.info(String.format(info, FileSizeUnit.countSize(bytes, unit), unit.name()));
			break;
		case REQUEST_BYTE_TRANSFER_EVENT:
			this.bytesWritten += bytes;
			unit = FileSizeUnit.countUnit(this.bytesWritten);
			info += String.format("%.3f%s", FileSizeUnit.countSize(this.bytesWritten, unit), unit.name());
			if (this.totalBytes != -1) {
				int percent = (int)(this.bytesWritten * 100.0 / this.totalBytes);
				info += " have been written, upload progress: " + percent + "%";
			} else {
				info += " have been written, upload ratio: unknown" + "(" + this.bytesWritten + "/...)";
			}
			LOGGER.info(info);
			break;
		case TRANSFER_COMPLETED_EVENT:
			this.succeed = true;
			unit = FileSizeUnit.countUnit(this.bytesWritten);
			info = "Succeed to upload %s, %.3f%s have been transferred in total";
			LOGGER.info(String.format(info, filename, FileSizeUnit.countSize(this.bytesWritten, unit), unit.name()));
			break;
		case TRANSFER_FAILED_EVENT:
			unit = FileSizeUnit.countUnit(this.bytesWritten);
			info = "Failed to upload %s, %.3f%s have been transferred";
			LOGGER.info(String.format(info, filename, FileSizeUnit.countSize(this.bytesWritten, unit), unit.name()));
			break;
		default:
			break;
		}

	}
	
	public boolean isSucceed() {
		return succeed;
	}

}
