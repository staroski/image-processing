package br.com.staroski.image;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public final class AverageImage {

	@SuppressWarnings("serial")
	private static final Component OBSERVER = new Component() {};

	private static BufferedImage create(int[] pixels, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	private static BufferedImage equalize(BufferedImage current, BufferedImage last) {
		final int lastWidth = last.getWidth(OBSERVER);
		final int lastHeight = last.getHeight(OBSERVER);
		final int currentWidth = current.getWidth(OBSERVER);
		final int currentHeight = current.getHeight(OBSERVER);
		if (currentWidth != lastWidth || currentHeight != lastHeight) {
			return resize(current, lastWidth, lastHeight);
		}
		return current;
	}

	private static int[] getPixels(BufferedImage image) {
		final int width = image.getWidth(OBSERVER);
		final int height = image.getHeight(OBSERVER);
		int size = width * height;
		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, width, height, new int[size], 0, width);
		pixelGrabber.startGrabbing();
		return (int[]) pixelGrabber.getPixels();
	}

	private static BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = resized.createGraphics();
		graphics.drawImage(image, 0, 0, width, height, OBSERVER);
		graphics.dispose();
		return resized;
	}

	private final GrayScale grayScale;

	public AverageImage(GrayScale mode) {
		this.grayScale = mode;
	}

	public BufferedImage compute(File[] imageFiles) throws IOException {
		int count = imageFiles.length;
		InputStream[] imageInputs = new InputStream[count];
		for (int i = 0; i < count; i++) {
			imageInputs[i] = new FileInputStream(imageFiles[i]);
		}
		return compute(imageInputs);
	}

	public BufferedImage compute(InputStream[] imageInputs) throws IOException {
		int count = imageInputs.length;
		BufferedImage last = ImageIO.read(imageInputs[0]);
		final int width = last.getWidth(OBSERVER);
		final int height = last.getHeight(OBSERVER);
		final int size = width * height;
		int[] average = getPixels(last);
		for (int index = 1; index < count; index++) {
			BufferedImage current = ImageIO.read(imageInputs[index]);
			current = equalize(current, last);
			int[] pixels = getPixels(current);
			for (int pixel = 0; pixel < size; pixel++) {
				average[pixel] = average[pixel] + pixels[pixel];
			}
			last = current;
		}
		for (int pixel = 0; pixel < size; pixel++) {
			average[pixel] = average[pixel] / count;
		}
		getGrayScale().compute(average);
		return create(average, width, height);
	}

	public BufferedImage compute(String[] imageFilePaths) throws IOException {
		int count = imageFilePaths.length;
		File[] imageFiles = new File[count];
		for (int i = 0; i < count; i++) {
			imageFiles[i] = new File(imageFilePaths[i]);
		}
		return compute(imageFiles);
	}

	public GrayScale getGrayScale() {
		return grayScale;
	}
}
