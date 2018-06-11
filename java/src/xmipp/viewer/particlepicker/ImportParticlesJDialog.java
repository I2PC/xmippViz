package xmipp.viewer.particlepicker;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import xmipp.jni.Filename;
import xmipp.utils.XmippDialog;
import xmipp.utils.XmippFileChooser;
import xmipp.utils.XmippMessage;
import xmipp.utils.XmippWindowUtil;
import xmipp.viewer.particlepicker.tiltpair.gui.ImportParticlesFromFilesTiltPairJDialog;
import xmipp.viewer.particlepicker.training.gui.SupervisedPickerJFrame;

public class ImportParticlesJDialog extends XmippDialog {

	protected ParticlePickerJFrame parent;
	protected JTextField sourcetf;
	protected JButton browsebt;
	protected JComboBox jcbFormat;
	public Format format = Format.Auto;
	protected XmippFileChooser xfc = null;
	protected String path;
	protected JFormattedTextField scaletf;
	protected JCheckBox invertxcb;
	protected JCheckBox invertycb;

	public static final String[] formatsString = new String[]{Format.Auto.toString(), Format.Xmipp24.toString(), Format.Xmipp30.toString(), Format.Xmipp301.toString(), Format.Eman.toString(), Format.Relion.toString()};
	public static final Format[] formatsList = { Format.Auto, Format.Xmipp24,
			Format.Xmipp30, Format.Xmipp301, Format.Eman, Format.Relion };
    private JTextField preffixtf;
    private JTextField suffixtf;

	public ImportParticlesJDialog(ParticlePickerJFrame parent) {
		super(parent, "Import Particles", true);
		this.parent = parent;
		xfc = new XmippFileChooser();
		if(parent instanceof SupervisedPickerJFrame)
			xfc.setFileSelectionMode(XmippFileChooser.FILES_AND_DIRECTORIES);
		else if (this instanceof ImportParticlesFromFilesTiltPairJDialog)
			xfc.setFileSelectionMode(XmippFileChooser.FILES_ONLY);
		else 
			xfc.setFileSelectionMode(XmippFileChooser.DIRECTORIES_ONLY);
		
		xfc.setMultiSelectionEnabled(false);
		initComponents();
	}// constructor
	
	

	@Override
	protected void createContent(JPanel panel) {
		setResizable(false);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		gbc.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("Format:"),
				XmippWindowUtil.getConstraints(gbc, 0, 0));
		panel.add(new JLabel("Source:"),
				XmippWindowUtil.getConstraints(gbc, 0, 1));

		
		/** Create a combobox with possible formats */
		jcbFormat = new JComboBox(formatsString);
		jcbFormat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
                int index = jcbFormat.getSelectedIndex();
				format = formatsList[index];
                suffixtf.setText(parent.getParticlePicker().emextensions.get(formatsList[index]));
				
			}
		});
		panel.add(jcbFormat, XmippWindowUtil.getConstraints(gbc, 1, 0));
		
		sourcetf = new JTextField(20);
		panel.add(sourcetf, XmippWindowUtil.getConstraints(gbc, 1, 1));
		
		browsebt = XmippWindowUtil.getIconButton("folderopen.gif", this);
		panel.add(browsebt, XmippWindowUtil.getConstraints(gbc, 2, 1));
		
        String tooltip = "Preffix added to micrograph name in coordinates file";
        JLabel preffixlb = new JLabel("Preffix:");
        preffixlb.setToolTipText(tooltip);
        panel.add(preffixlb, XmippWindowUtil.getConstraints(gbc, 0, 2));
        preffixtf = new JTextField(20);
        preffixtf.setToolTipText(tooltip);
        panel.add(preffixtf, XmippWindowUtil.getConstraints(gbc, 1, 2));
        tooltip = "Suffix added to micrograph name in coordinates file (eg:_filt.pos)";
        JLabel suffixlb = new JLabel("Suffix:");
        panel.add(suffixlb, XmippWindowUtil.getConstraints(gbc, 0, 3));
        suffixlb.setToolTipText(tooltip);
        suffixtf = new JTextField(20);
        suffixtf.setToolTipText(tooltip);
		panel.add(suffixtf, XmippWindowUtil.getConstraints(gbc, 1, 3));
                
		panel.add(new JLabel("Scale To:"),	XmippWindowUtil.getConstraints(gbc, 0, 4));
		scaletf = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.ENGLISH));
		scaletf.setColumns(3);
		scaletf.setValue(1);
		panel.add(scaletf, XmippWindowUtil.getConstraints(gbc, 1, 4));
		
		panel.add(new JLabel("Invert X:"),
		XmippWindowUtil.getConstraints(gbc, 0, 5));
		invertxcb = new JCheckBox();
		panel.add(invertxcb, XmippWindowUtil.getConstraints(gbc, 1, 5));
		
		panel.add(new JLabel("Invert Y:"),
		XmippWindowUtil.getConstraints(gbc, 0, 6));
		invertycb = new JCheckBox();
		panel.add(invertycb, XmippWindowUtil.getConstraints(gbc, 1, 6));
                
        sourcetf.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String path = sourcetf.getText();
				boolean isDir = new File(path).isDirectory();
				preffixtf.setEnabled(isDir);
                suffixtf.setEnabled(isDir);
				
			}
		});       
		
	}// function createContent

	@Override
	public void handleActionPerformed(ActionEvent evt) {
			browseDirectory(sourcetf);
		
	}// function actionPerformed

	protected void browseDirectory(JTextField sourcetf) {
		int returnVal = xfc.showOpenDialog(this);
		try {
			if (returnVal == XmippFileChooser.APPROVE_OPTION) {
				path = xfc.getSelectedPath();
				sourcetf.setText(path);
                preffixtf.setEnabled(xfc.getSelectedFile().isDirectory());
                suffixtf.setEnabled(xfc.getSelectedFile().isDirectory());
                                
			}
		} catch (Exception ex) {
			showException(ex);
		}

	}

	@Override
	public void handleOk() {
		
			path = sourcetf.getText().trim();

			if (path == null || path.equals(""))
				showError(XmippMessage.getEmptyFieldMsg("source"));
			else if (!existsSelectedPath())
				showError(XmippMessage.getPathNotExistsMsg(path));
			else
			{
				String result = importParticles();
				if(result != null && !result.equals(""))
					XmippDialog.showInfo(this.parent, result);

			}
		
	}
	
	protected String importParticles()
	{
            try {
                    return parent.importParticles(format, path, preffixtf.getText(), suffixtf.getText(), ((Number)scaletf.getValue()).floatValue(), invertxcb.isSelected(), invertycb.isSelected());
            } catch (Exception e) {
                    XmippDialog.showInfo(parent, e.getMessage());
            }
            return null;

	}
	
	
	
	private boolean existsSelectedPath(){
			return Filename.exists(path);
		
	}//function existsSelectedPaths

	
	


}
