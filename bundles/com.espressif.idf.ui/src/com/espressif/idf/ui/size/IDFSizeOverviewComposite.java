package com.espressif.idf.ui.size;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.espressif.idf.core.logging.Logger;

public class IDFSizeOverviewComposite {

    private FormToolkit toolkit;
    private Composite overviewComp;
    private JSONObject overviewJson;
    private Font boldFont;

    public void createPartControl(Composite parent, IFile file, String targetName) {
        toolkit = new FormToolkit(parent.getDisplay());
        Form form = toolkit.createForm(parent);
        toolkit.decorateFormHeading(form);
        form.setText(Messages.IDFSizeOverviewComposite_ApplicatoinMemoryUsage);
        form.getBody().setLayout(new GridLayout());

        Section ec2 = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
        ec2.setText(Messages.IDFSizeOverviewComposite_Overview);
        ec2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        overviewComp = new Composite(ec2, SWT.NONE);
        overviewComp.setLayout(new GridLayout(2, false));
        ec2.setClient(overviewComp);

        overviewJson = getIDFSizeOverviewData(file, targetName);
        renderOverviewSection();
    }

    private void renderOverviewSection() {
        Label header1 = toolkit.createLabel(overviewComp, Messages.IDFSizeOverviewComposite_MemoryRegion);
        FontDescriptor boldDescriptor = FontDescriptor.createFrom(header1.getFont()).setStyle(SWT.BOLD);
        boldFont = boldDescriptor.createFont(header1.getDisplay());
        Label header2 = toolkit.createLabel(overviewComp, Messages.IDFSizeOverviewComposite_UsedSize);
        header1.setFont(boldFont);
        header2.setFont(boldFont);

        long totalUsed = 0;
        long totalFree = 0;

        JSONArray layoutArray = (JSONArray) overviewJson.get(IDFSizeConstants.LAYOUT);
        for (Object obj : layoutArray) {
            JSONObject section = (JSONObject) obj;
            String sectionName = (String) section.get(IDFSizeConstants.NAME);
            long used = (long) section.get(IDFSizeConstants.USED);
            long free = (long) section.get(IDFSizeConstants.FREE);

            totalUsed += used;
            totalFree += free;

            toolkit.createLabel(overviewComp, sectionName + ":"); //$NON-NLS-1$
            Label value = toolkit.createLabel(overviewComp, used / 1024 + " KB"); //$NON-NLS-1$
            value.setFont(boldFont);
        }

        toolkit.createLabel(overviewComp, Messages.IDFSizeOverviewComposite_TotalSize);
        Label totalLbl = toolkit.createLabel(overviewComp, (totalUsed + totalFree) / 1024 + " KB"); //$NON-NLS-1$
        totalLbl.setFont(boldFont);

        overviewComp.layout(true, true);
    }

    protected JSONObject getIDFSizeOverviewData(IFile file, String targetName) {
        try {
            return new IDFSizeDataManager().getIDFSizeOverview(file, targetName);
        } catch (Exception e) {
            Logger.log(e);
            return null;
        }
    }
}
