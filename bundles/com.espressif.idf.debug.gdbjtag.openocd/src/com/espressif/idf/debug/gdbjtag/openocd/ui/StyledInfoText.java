package com.espressif.idf.debug.gdbjtag.openocd.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class StyledInfoText
{
	private StyledText styledText;
	private String text;
	private StyleRange linkStyleRange;

	public StyledInfoText(Composite parent)
	{
		text = String.format(
				"%1$s Text fields with an icon %1$s use dynamic variables. Click %1$s to see actual values. If the configuration is from an older version, click %2$s to populate fields with dynamic variables.",
				"\uFFFC", "Restore defaults");

		styledText = new StyledText(parent, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		var gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 100;
		styledText.setLayoutData(gd);
		Color grayColor = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		styledText.setBackground(grayColor);
		styledText.setText(text);

		linkStyleRange = new StyleRange(text.indexOf("Restore defaults"), "Restore defaults".length(), null, null);
		linkStyleRange.underline = true;
		linkStyleRange.underlineStyle = SWT.UNDERLINE_LINK;
		styledText.setStyleRange(linkStyleRange);

		addAllListeners();
	}

	public void setMouseListenerAction(Runnable action)
	{
		styledText.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseUp(MouseEvent e)
			{
				int offset = styledText.getOffsetAtPoint(new Point(e.x, e.y));
				if (offset >= linkStyleRange.start && offset < linkStyleRange.start + linkStyleRange.length)
				{
					action.run();
				}
			}
		});

	}

	public int getOffsetAtPoint(Point point)
	{
		return styledText.getOffsetAtPoint(point);
	}

	private void addAllListeners()
	{
		Image buttonShowImage = ImageDescriptor.createFromURL(getClass().getResource("/icons/obj16/show.png")) //$NON-NLS-1$
				.createImage();
		Image infoButtonImage = JFaceResources.getImage("dialog_messasge_info_image");
		Image[] images = new Image[] { infoButtonImage, buttonShowImage, buttonShowImage };
		int[] offsets = new int[images.length];
		int lastOffset = 0;
		for (int i = 0; i < images.length; i++)
		{
			int offset = text.indexOf("\uFFFC", lastOffset);
			offsets[i] = offset;
			addImage(images[i], offset, styledText);
			lastOffset = offset + 1;
		}
		addVerifyListener(images, offsets);
		addPaintObjectListener(images, offsets);
		addDisposeListener(images);
	}

	private void addDisposeListener(Image[] images)
	{
		styledText.addDisposeListener(e -> {
			for (Image image : images)
			{
				image.dispose();
			}
		});
	}

	private void addVerifyListener(Image[] images, int[] offsets)
	{
		styledText.addVerifyListener(e -> {
			int start = e.start;
			int replaceCharCount = e.end - e.start;
			int newCharCount = e.text.length();
			for (int i = 0; i < offsets.length; i++)
			{
				int offset = offsets[i];
				if (start <= offset && offset < start + replaceCharCount)
				{
					if (images[i] != null && !images[i].isDisposed())
					{
						images[i].dispose();
						images[i] = null;
					}
					offset = -1;
				}
				if (offset != -1 && offset >= start)
					offset += newCharCount - replaceCharCount;
				offsets[i] = offset;
			}
		});
	}

	private void addPaintObjectListener(Image[] images, int[] offsets)
	{
		styledText.addPaintObjectListener(event -> {
			GC gc = event.gc;
			StyleRange style = event.style;
			int start = style.start;
			for (int i = 0; i < offsets.length; i++)
			{
				int offset = offsets[i];
				if (start == offset)
				{
					Image image = images[i];
					int x = event.x;
					int y = event.y + event.ascent - style.metrics.ascent + 3;
					gc.drawImage(image, x, y);
				}
			}
		});
	}

	private void addImage(Image image, int offset, StyledText styledText)
	{
		StyleRange style = new StyleRange();
		style.start = offset;
		style.length = 1;
		Rectangle rect = image.getBounds();
		style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		styledText.setStyleRange(style);
	}
}
