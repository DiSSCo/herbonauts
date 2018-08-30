package libs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class Images extends play.libs.Images {

	public static class Dimensions {
		public int width;
		public int height;

		public Dimensions(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}


	public static BufferedImage removeTransparency(BufferedImage img) {

		BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = copy.createGraphics();
		g2d.setColor(Color.WHITE); // Or what ever fill color you want...
		g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();

		return copy;
	}

	public static byte[] compress(File src) {
	    try {
	        // Retrieve jpg image to be compressed
	        BufferedImage rawRendImage = ImageIO.read(src);

			BufferedImage rendImage = removeTransparency(rawRendImage);

	        // Find a jpeg writer
	        ImageWriter writer = null;
	        Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
	        if (iter.hasNext()) {
	            writer = (ImageWriter)iter.next();
	        }


			//BufferedImage image = new BufferedImage(rendImage.getWidth(), rendImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			//Graphics2D g = image.createGraphics();
			//g.setComposite(AlphaComposite.Src);
			//g.drawImage(image, 0, 0, Color.WHITE, null);
			//g.drawRenderedImage(rendImage, null);
			//g.get
//
	        // Prepare output file
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
	        writer.setOutput(ios);

	        // Set the compression quality
	        ImageWriteParam iwparam = writer.getDefaultWriteParam();
	        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
			iwparam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
	        iwparam.setCompressionQuality(1);

	        // Write the image
	        writer.write(null, new IIOImage(rendImage, null, null), iwparam);

	        // Cleanup
	        ios.flush();
	        writer.dispose();
	        ios.close();

	        return bos.toByteArray();
	        
	    } catch (IOException e) {
	    	throw new RuntimeException(e);
	    }
	}

	public static boolean isImageReadable(File image) {
		try {
			BufferedImage bufImg = ImageIO.read(image);
			return (bufImg != null && bufImg.getWidth() > 0);
		} catch (Exception e) {
			return false;
		}
		
	}
	
	public static Dimensions dimensions(File image) {

		try {
			BufferedImage source = ImageIO.read(image);
			int w = source.getWidth();
			int h = source.getHeight();

			return new Dimensions(w, h);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Crop le plus grand carré possible au centre exemple: - image 300x200 =>
	 * 200x200 centré à 150, 100
	 * 
	 * @param original
	 * @param dest
	 */
	public static void squarify(File original, File dest) {
		Dimensions dims = dimensions(original);

		// Il y a sans doute plus élégant
		if (dims.width < dims.height) {
			int size = dims.width;
			int top = (dims.height - size) / 2;
			crop(original, dest, 0, top, size, size + top);
		} else {
			int size = dims.height;
			int left = (dims.width - size) / 2;
			crop(original, dest, left, 0, size + left, size);
		}

	}

	public static void centeredCrop(File original, File dest, int w, int h) {
		Dimensions dims = dimensions(original);

		if (dims.width < w) {
			resize(original, original, w, -1);
			dims = dimensions(original);
		}
		if (dims.height < h) {
			resize(original, original, -1, h);
			dims = dimensions(original);
		}

		int x = (dims.width - w) / 2;
		int y = (dims.height - h) / 2;

		crop(original, dest, x, y, x + w, y + h);

	}
}
