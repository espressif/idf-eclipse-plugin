package com.espressif.idf.ui;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.CustomizableIntroPart;

public class EspIntroPart implements IIntroPart
{

	private CustomizableIntroPart customizebleIntropart;

	public EspIntroPart()
	{
		customizebleIntropart = new CustomizableIntroPart();
	}
	public <T> T getAdapter(Class<T> adapter)
	{
		return customizebleIntropart.getAdapter(adapter);
	}

	public IIntroSite getIntroSite()
	{
		return customizebleIntropart.getIntroSite();
	}

	public void init(IIntroSite site, IMemento memento) throws PartInitException
	{
		customizebleIntropart.init(site, null);
	}

	public void standbyStateChanged(boolean standby)
	{
		customizebleIntropart.standbyStateChanged(standby);

	}

	public void saveState(IMemento memento)
	{
		customizebleIntropart.saveState(memento);
	}

	public void addPropertyListener(IPropertyListener listener)
	{
		customizebleIntropart.addPropertyListener(listener);

	}

	public void createPartControl(Composite parent)
	{
		// TODO Auto-generated method stub

	}

	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	public Image getTitleImage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void removePropertyListener(IPropertyListener listener)
	{
		// TODO Auto-generated method stub

	}

	public void setFocus()
	{
		// TODO Auto-generated method stub

	}

}
