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
 * Jpeg 画像の Exif を読み取る便利関数群。
 * drewnoakes氏の metadata-extractor ライブラリを自分が使いやすい関数に
 * 変換したもの
 * https://github.com/drewnoakes/metadata-extractor/releases
 * ver 2.9.1
 */
public class MyExifUtils {
	
	// 使わない。Metadata 取得のサンプルとして残置
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
	 * 指定された JPEG ファイルの撮影日を取得します。
	 * 可能であれば、EXIF の Date/Time を取得、
	 * なければ JPEG Metadata の Modified Date を取得します。
	 * これもない場合、null が返却されます。
	 */
	public static Date getJpegDate(String pname) throws IOException {
		try {
			File file = new File(pname);
			
			Metadata metadata = JpegMetadataReader.readMetadata(file);
			
			Date date = null;
			
			// Exif フィールドからの取得を試みる
			// こっちは9時間ずれるようだ。補正する。
			// 海外時間で撮った写真はおそらく狂う。
			ExifDirectoryBase directory = metadata.getFirstDirectoryOfType(ExifDirectoryBase.class);
			if (directory != null) {
				date = directory.getDate(ExifDirectoryBase.TAG_DATETIME);
				if (date != null) return new Date(date.getTime() - 9 * 60 * 60 * 1000); // 補正
			}
			
			// ない場合、File フィールドからの取得を試みる
			FileMetadataDirectory fdir = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
			if (fdir == null) return null;
			date = fdir.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
			
			return date; // may be null.
		} catch (Exception e) { //ImageProcessingException, MetadataException, 
			throw new IOException(e.toString());
		}
	}

}
