package com.op;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class InstagramSplitter extends Base {

	private static InstagramSplitter splitter = new InstagramSplitter();

	private String ip = "VirgaCol_CirclePack";
	private String ipFile = host + ip;
	private String opFile = ip + "_OUT";

	private static final int TYPE_ZOOM = 0;
	private static final int TYPE_INST = 1;
	private static final int TYPE_ZOOM_TR = 2;
	private static final int TYPE_ZOOM_GIF = 3;
	private static final int TYPE_LIN_GIF = 4;
	private static final String TYPE_ZOOM_STR = "ZOOM";
	private static final String TYPE_ZOOM_TR_STR = "ZOOM_TR";
	private static final String TYPE_INST_STR = "INST";
	private static final String TYPE_ZOOM_GIF_STR = "GIF";
	private static final String TYPE_LIN_GIF_STR = "LGIF";
	private static final String[] TYPES_STR = { TYPE_ZOOM_STR, TYPE_ZOOM_TR_STR, TYPE_INST_STR, TYPE_ZOOM_GIF_STR,
			TYPE_LIN_GIF_STR };
	private int type = TYPE_ZOOM;

	private BufferedImage ibi;
	private BufferedImage obi;
	private Graphics2D opG;
	private int d = -1;
	private int w = -1;
	private int h = -1;

	public static void main(String[] args) throws IOException {
		splitter.initFiles();
		splitter.draw();
	}

	private void draw() throws IOException {
		if (type == TYPE_INST) {
			splitInstagram();
		} else if (type == TYPE_ZOOM) {
			splitZoom();
		} else if (type == TYPE_ZOOM_GIF) {
			splitZoomGif();
		} else if (type == TYPE_LIN_GIF) {
			splitLinearGif();
		} else if (type == TYPE_ZOOM_TR) {
			//splitZoomTR();
			splitZoom(0, 0, 2000, 500, 500, 250, 5);
		}
	}

	private void splitInstagram() throws IOException {
		int d = 4000 / 3;
		int i = 0;
		for (int y = 0; y < 4000 - 10; y = y + d) {
			for (int x = 0; x < 4000 - 10; x = x + d) {
				draw(x, y, d);
				save(i + 1);
				i++;
			}
		}
	}

	private void splitZoom() throws IOException {
		draw(0, 0, d);
		save(1);

		int r = d / 4;
		draw(r, r, d / 2);
		save(2);

		r = 3 * d / 8;
		draw(r, r, d / 4);
		save(3);

		r = (int) ((d) * 1750d / 4000);
		draw(r, r, d / 8);
		save(4);

		r = (int) ((d) * 1900d / 4000);
		draw(r, r, d / 12);
		save(5);
	}

	private void splitZoomGif() throws IOException {
		int i = 0;
		int l = 200;
		for (int n = d; n > l; n = n - l) {
			draw((d - n) / 2, (d - n) / 2, n);
			save(i);
			i++;
		}
	}

	private void splitLinearGif() throws IOException {
		int st = 50500;
		int en = 51500;
		double num = 100;
		int dd = (int) ((en - st) / num);
		int i = 0;
		for (int x = st; x < en; x = x + dd) {
			draw(x, 0, 576);
			save(i);
			i++;
		}
	}

	private void splitZoom(double x1, double y1, double d1, double x2, double y2, double d2, double num) throws IOException {
		int dd = (int)((d1-d2)/(num-1));
		int xd = (int)((x2-x1)/num);
		int yd = (int)((y2-y1)/num);
		for (double i=1; i<=num; i++) {
			draw((int)(xd*(i-1)), (int)(yd*(i-1)), (int)d1 - dd*((int)(i-1)));
			save((int) i);
		}
	}

	private void splitZoomTR() throws IOException {
		draw(0, 0, d);
		save(1);

		draw(d / 2, 0, d / 2);
		save(2);

		draw(2 * d / 3, 0, d / 3);
		save(3);

		draw(4 * d / 5, 0, d / 5);
		save(4);

		draw(7 * d / 8, 0, d / 8);
		save(5);
	}

	private void splitZoomCorner() throws IOException {
		int i = 0;
		int x = 375, y = 375, r = 250;
		draw(x, y, r);
		save(i);
		i = i + 1;
		r = r + 250;
		x = x - 75;
		y = y - 75;
		draw(x, y, r);
		save(i);

		i = i + 1;
		r = r + 250;
		x = x - 75;
		y = y - 75;
		draw(x, y, r);
		save(i);

		i = i + 1;
		r = r + 500;
		x = x - 75;
		y = y - 75;
		draw(x, y, r);
		save(i);

		i = i + 1;
		draw(0, 0, 2000);
		save(i);

		i = i + 1;
		draw(0, 0, 4000);
		save(i);
	}

	private void savePNG(int i) throws IOException {
		File op1 = new File(host + opFile + TYPES_STR[type] + "_" + i + ".png");
		ImageIO.write(obi, "png", op1);
		System.out.println("Saved " + op1.getPath());
	}

	private void save(int i) throws IOException {
		String fName = "";
		if (type == TYPE_LIN_GIF) {
			fName = host + ip + "_" + TYPES_STR[type] + "_" + i + ".jpg";
		} else {
			fName = host + opFile + TYPES_STR[type] + "_" + i + ".jpg";
		}
		File op1 = new File(fName);
		ImageIO.write(obi, "jpg", op1);
		System.out.println("Saved " + op1.getPath());
	}

	private void draw(int x1, int y1, int len) {
		double fr = (double) w / (double) len;
		AffineTransform at = AffineTransform.getScaleInstance(fr, fr);

		BufferedImage sub = ibi.getSubimage(x1, y1, len, len);
		opG.drawImage(sub, at, null);
	}

	private void initFiles() throws IOException {
		if (type == TYPE_LIN_GIF) {
			ibi = ImageIO.read(new File(host + ip + "_LINE.png"));
			w = 250;
			h = w;

		} else {
			ibi = ImageIO.read(new File(ipFile + ".png"));
			d = ibi.getWidth();
			w = d / 8;
			h = w;
		}
		obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		opG = (Graphics2D) obi.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);
	}
}
