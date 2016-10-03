package abdom.image.exif;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.file.FileMetadataDirectory;

/**
 * Jpeg �摜�� Exif ��ǂݎ��֗��֐��Q�B
 * drewnoakes���� metadata-extractor ���C�u�������������g���₷���֐���
 * �ϊ���������
 * https://github.com/drewnoakes/metadata-extractor/releases
 * ver 2.9.1
 */
public class MyExifUtils {
	
	// �g��Ȃ��BMetadata �擾�̃T���v���Ƃ��Ďc�u
	public static void view(String pname)
				throws ImageProcessingException, IOException {
		File file = new File(pname);
		
		Metadata metadata = ImageMetadataReader.readMetadata(file);
		
		for (Directory directory : metadata.getDirectories()) {
		    for (Tag tag : directory.getTags()) {
		        System.out.format("[%s] - %s = %s",
		            directory.getName(), tag.getTagName(), tag.getDescription());
		        System.out.println();
		    }
		    if (directory.hasErrors()) {
		        for (String error : directory.getErrors()) {
		            System.err.format("ERROR: %s", error);
			        System.out.println();
		        }
		    }
		}
	}
	
	/**
	 * �w�肳�ꂽ JPEG �t�@�C���̎B�e�����擾���܂��B
	 * �\�ł���΁AEXIF �� Date/Time ���擾�A
	 * �Ȃ���� JPEG Metadata �� Modified Date ���擾���܂��B
	 * ������Ȃ��ꍇ�Anull ���ԋp����܂��B
	 */
	public static Date getJpegDate(String pname) throws IOException {
		try {
			File file = new File(pname);
			
			Metadata metadata = JpegMetadataReader.readMetadata(file);
			
			Date date = null;
			
			// Exif �t�B�[���h����̎擾�����݂�
			// ��������9���Ԃ����悤���B�␳����B
			// �C�O���ԂŎB�����ʐ^�͂����炭�����B
			ExifDirectoryBase directory = metadata.getFirstDirectoryOfType(ExifDirectoryBase.class);
			if (directory != null) {
				date = directory.getDate(ExifDirectoryBase.TAG_DATETIME);
				if (date != null) return new Date(date.getTime() - 9 * 60 * 60 * 1000); // �␳
			}
			
			// �Ȃ��ꍇ�AFile �t�B�[���h����̎擾�����݂�
			FileMetadataDirectory fdir = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
			if (fdir == null) return null;
			date = fdir.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
			
			return date; // may be null.
		} catch (Exception e) { //ImageProcessingException, MetadataException, 
			throw new IOException(e.toString());
		}
	}

}
