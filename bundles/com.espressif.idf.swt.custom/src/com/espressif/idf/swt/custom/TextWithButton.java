package com.espressif.idf.swt.custom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.logging.Logger;

public class TextWithButton
{

	private Text text;
	private Label button;
	private Composite baseComposite;

	public TextWithButton(final Composite parent, int style)
	{
		baseComposite = new Composite(parent, style);
		baseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		final GridLayout baseCompositeGridLayout = new GridLayout(2, false);
		baseCompositeGridLayout.marginHeight = 0;
		baseCompositeGridLayout.marginWidth = 0;
		baseComposite.setLayout(baseCompositeGridLayout);

		baseComposite.setBackground(new Color(parent.getDisplay(), new RGB(255, 255, 255)));
		baseComposite.setBackgroundMode(SWT.INHERIT_FORCE);

		text = new Text(baseComposite, SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Image buttonShowImage = ImageDescriptor.createFromURL(getClass().getResource("/icons/show.png")) //$NON-NLS-1$
				.createImage();
		Image buttonHideImage = ImageDescriptor.createFromURL(getClass().getResource("/icons/hide.png")) //$NON-NLS-1$
				.createImage();

		button = new Label(baseComposite, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		button.setImage(buttonShowImage);
		button.addMouseListener(new MouseAdapter()
		{
			String textWhenButtonIsPressed;

			@Override
			public void mouseUp(final MouseEvent e)
			{
				button.setImage(buttonShowImage);
				text.setText(textWhenButtonIsPressed);
			}

			@Override
			public void mouseDown(final MouseEvent e)
			{
				button.setImage(buttonHideImage);
				textWhenButtonIsPressed = text.getText();

				try
				{
					text.setText(VariablesPlugin.getDefault().getStringVariableManager()
							.performStringSubstitution((textWhenButtonIsPressed), false));
				}
				catch (CoreException e1)
				{
					Logger.log(e1);
				}
			}

		});
		button.addDisposeListener(e -> {
			buttonHideImage.dispose();
			buttonShowImage.dispose();
		});
	}

	public void insert(String variableExpression)
	{
		text.insert(variableExpression);
	}

	public void addMouseTrackListener(MouseTrackAdapter mouseTrackAdapter)
	{
		text.addMouseTrackListener(mouseTrackAdapter);
	}

	public String getText()
	{
		return text.getText();
	}

	public Control getControl()
	{
		return text;
	}

	public void setToolTipText(String trim)
	{
		text.setToolTipText(trim);

	}

	public void addModifyListener(ModifyListener modifyListener)
	{
		text.addModifyListener(modifyListener);

	}

	public void setLayoutData(GridData gd)
	{
		baseComposite.setLayoutData(gd);
	}

	public void setText(String str)
	{
		text.setText(str);

	}

	public void setEnabled(boolean enabled)
	{
		text.setEnabled(enabled);
		button.setEnabled(enabled);
	}

	public void setParent(Group group)
	{
		baseComposite.setParent(group);

	}

	public void addTraverseListener(TraverseListener traverseListener)
	{
		baseComposite.addTraverseListener(traverseListener);

	}
}
