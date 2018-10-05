package fr.mnhn.herbonautes.tiles;


import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {


	public static String[] LINK_FILES = new String[] { "tile_0_0_0.jpg", "original.jpg", "crop.jpg" };
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		System.out.println("Herbonautes tiles batch");
		System.out.println("-----------------------");
		if (args.length > 0) {
			System.out.println("Using conf file " + args[0]);
			Conf.init(args[0]);
		} else {
			System.out.println("Usage : java -jar herbonautes.jar /path/to/conf.properties");
			System.exit(1);
		}
		for (;;) {
			Specimen specimen = SpecimenRepository.get().getNextUntiled();
			if (specimen == null) {
				System.out.print("No specimen");
				return;
			}

			// TODO GEstion des repertoires avec File()
			String imageBaseDir = Conf.IMAGES_ROOT_DIRECTORY + specimen.getStringPath() + "/";
			String originalImageFile = imageBaseDir + "1/original.jpg";

			// Test exists
			// TODO remove hardcoded file name
			String existingTileImagePath = imageBaseDir + "tile_2_0_0.jpg";
us
git
			if (new File(existingTileImagePath).exists()) {
				System.out.print("[" + specimen.getStringPath() + "]");
				System.out.println(" >> Already exists >> SKIP");
				SpecimenRepository.get().markAsTiled(specimen);
				continue;
			}


			// Change for each media for specimen



			try {
				System.out.print("[" + specimen.getStringPath() + "]");

				List<SpecimenMedia> mediaList = SpecimenRepository.get().getMediaList(specimen.getId());
				System.out.println(" >> " + mediaList.size() + " media");

				for (SpecimenMedia media : mediaList) {

					try {


						String mediaBaseDir = Conf.IMAGES_ROOT_DIRECTORY + specimen.getStringPath() + "/";
						if (media.getMediaNumber() > 1) {
							// First media images at root
							mediaBaseDir += media.getMediaNumber() + "/";
						}
						String mediaOriginalImageFile = mediaBaseDir + "original.jpg";

						System.out.print("  - media " + media.getMediaNumber());

						System.out.print(" >> downloading");
						IO.writeFile(HTTP.get(media.getUrl()), new File(mediaOriginalImageFile));

						long originalSize = FileUtils.sizeOf(new File(mediaOriginalImageFile)) / 1000;
						System.out.print(" (" + originalSize + " Kb)");

						System.out.print(" >> tiling");
						long[] dims = tile(mediaBaseDir);

						//TILEWIDTH = ? ,TILEHEIGHT = ?

						SpecimenRepository.get().saveMediaDims(media, dims[0], dims[1]);
						SpecimenRepository.get().markMediaAsTiled(media);


						long finalSize = FileUtils.sizeOfDirectory(new File(imageBaseDir)) / 1000;
						System.out.println(" >> SUCCESS (" + finalSize + " Kb)");

					} catch (Exception e) {

						SpecimenRepository.get().markMediaAsError(media);
						System.out.println(" >> ERROR (" + e.getMessage() + ")");
						//e.printStackTrace();

						throw(e);
					}


				}



				System.out.println("All media tiled");

				//System.out.println(" >> Create symbolic links for images");
//
//
				//for (String linkFileName : LINK_FILES) {
				//	String target = imageBaseDir + "1/" + linkFileName;
				//	String link = imageBaseDir + linkFileName;
				//	Runtime.getRuntime().exec(new String[]{"ln", "-s", target, link});
				//}

				SpecimenRepository.get().markAsTiled(specimen);
				
			} catch (Exception e) {
				
				SpecimenRepository.get().markAsError(specimen);
				System.out.println(" >> ERROR (" + e.getMessage() + ")");
				e.printStackTrace();
			}
			
		}
      
	}
	
	
	public static long[] tile(String baseDir) throws IOException {
		System.gc();
		
		long[] dims = new long[2];


		long start = System.currentTimeMillis();
		BufferedImage original = ImageIO.read(new File(baseDir + "original.jpg"));
		
		int zoom = 4;
		
		int tile_w = powerApproach(original.getWidth(), 1 << zoom);
		int tile_h = powerApproach(original.getHeight(), 1 << zoom);
		
		System.out.print(" ; dims : " + tile_w + "x" + tile_h);
		dims[0] = tile_w;
		dims[1] = tile_h;
		
	
		System.out.print(" ; zooms :");
		BufferedImage tile = null;
		for (int z = 0 ; z <= zoom ; z++) {
			System.out.print(" " + z);
			int n = 1 << z;
			BufferedImage img = resize(original, tile_w*n, tile_h*n);
			for (int i = 0 ; i < n ; i++) {
				for (int j = 0 ; j < n; j++) {
					tile = crop(img, i * tile_w, j * tile_h, tile_w, tile_h);
					ImageIO.write(tile, "jpg", new File(tileFileName(baseDir, z, i, j))); 
					tile = null;
				}
			}
		}
		//}
		
		// Crop (pour page accueil)
		int cropW = original.getWidth() / 3;
		int cropH = cropW * Conf.CROP_HEIGHT / Conf.CROP_WIDTH;
		BufferedImage img = crop(original, 
				original.getWidth() / 3, 
				original.getHeight() / 3, 
				cropW, 
				cropH);
		tile = resize(img, Conf.CROP_WIDTH, Conf.CROP_HEIGHT);
		ImageIO.write(tile, "jpg", new File(baseDir + "crop.jpg")); 
		
		System.out.print(" ; done in " + (System.currentTimeMillis() - start) + " ms");
		return dims;
	}

	public static String tileFileName(String baseDir, int zoom, int x, int y) {
		return baseDir + "tile_" + zoom + "_" + x + "_" + y + ".jpg";
	}
	
	public static BufferedImage square(BufferedImage in) {
		
		return null;
	}
	
	public static BufferedImage resize(BufferedImage source, int w, int h) {
		BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(source, 0, 0, w, h, null); 
        g.dispose();
        return scaled;
	}
	
	public static BufferedImage crop(BufferedImage source, int x, int y, int w, int h) {
		return source.getSubimage(x, y, w, h);
	}
	
	public static int powerApproach(int dim, int div) {
		int tile = dim / div;
		return tile + (dim % tile == div ? 0 : 1);
	}
	

}
