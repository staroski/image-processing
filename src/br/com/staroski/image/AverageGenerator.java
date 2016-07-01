package br.com.staroski.image;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public final class AverageGenerator {

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
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = resized.createGraphics();
		graphics.drawImage(image, 0, 0, width, height, OBSERVER);
		graphics.dispose();
		return resized;
	}

	private final List<InputStream> inputs = new LinkedList<>();

	private BufferedImage averageImage;

	public AverageGenerator() {

	}

	public void add(InputStream input) {
		inputs.add(input);
	}

	public BufferedImage compute(GrayScale grayScale) throws IOException {
		int count = inputs.size();
		InputStream[] imageInputs = inputs.toArray(new InputStream[count]);
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
		grayScale.compute(average);
		averageImage = create(average, width, height);
		return averageImage;
	}

	public BufferedImage getAverageImage() {
		return averageImage;
	}

	public void save(OutputStream output, String format) throws IOException {
		ImageOutputStream out = ImageIO.createImageOutputStream(output);
		if (!ImageIO.write(getAverageImage(), format, out)) {
			throw new IOException(String.format("No writer found for format \"%s\"", format));
		}
	}
}
