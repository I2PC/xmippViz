/***************************************************************************
 * Authors:     J.M. de la Rosa Trevin (jmdelarosa@cnb.csic.es)
 *
 *
 * Unidad de  Bioinformatica of Centro Nacional de Biotecnologia , CSIC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307  USA
 *
 *  All comments concerning this program package may be sent to the
 *  e-mail address 'xmipp@cnb.csic.es'
 ***************************************************************************/

package xmipp.viewer.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import xmipp.utils.XmippDialog;
import xmipp.utils.XmippWindowUtil;
import xmipp.viewer.models.ColumnInfo;

public class ColumnsJDialog extends XmippDialog {
	private static final long serialVersionUID = 1L;
	public static final int VISIBLE_COL = 1;
	public static final int RENDER_COL = 2;
	public static final int LABEL_COL = 0;
	private JTable tableColumns;
	private JButton btnUp;
	private JButton btnDown;
	private ColumnsTableModel model;
	// This will be used for check for results from the dialog
	private List<ColumnInfo> rows;
	boolean fireEvent = true;

	public ColumnsJDialog(GalleryJFrame parent) {
		super(parent, "Columns", true);
		initComponents();
	}// constructor ColumnsJDialog

	public List<ColumnInfo> getColumnsResult() {
		return rows;
	}

	@Override
	protected void createContent(JPanel panel){
		setResizable(false);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.WEST;

		JPanel groupstbpn = new JPanel();
		JScrollPane sp = new JScrollPane();
		groupstbpn.setBorder(BorderFactory
				.createTitledBorder("Column properties"));
		groupstbpn.add(sp);
		sp.setOpaque(true);
		model = new ColumnsTableModel(((GalleryJFrame)parent).getData().getLabelsInfo());
		TableCellRenderer renderColumneRenderer = new RenderColumnRenderer();
		tableColumns = new JTable(model){
			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				if (column == RENDER_COL) {
					return renderColumneRenderer;
				} else {
					// else...
					return super.getCellRenderer(row, column);
				}
			}
		};
		tableColumns
				.setPreferredScrollableViewportSize(new Dimension(350, 200));
		sp.setViewportView(tableColumns);
		panel.add(groupstbpn, XmippWindowUtil.getConstraints(gbc, 0, 0));

		JPanel panelUpDown = new JPanel();
		panelUpDown.setLayout(new GridBagLayout());
		gbc.insets = new Insets(0, 0, 5, 5);
		btnUp = XmippWindowUtil.getIconButton("up.gif", this);
		panelUpDown.add(btnUp, XmippWindowUtil.getConstraints(gbc, 0, 0));
		btnDown = XmippWindowUtil.getIconButton("down.gif", this);
		panelUpDown.add(btnDown, XmippWindowUtil.getConstraints(gbc, 0, 1));
		panel.add(panelUpDown, XmippWindowUtil.getConstraints(gbc, 1, 0));
		// this buttons will be enabled after selection
		enableUpDown(false);
		// listen to selection changes (only one row selected)
		tableColumns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableColumns.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						enableUpDown(true);
					}
				});

		addHeaderClickListener();

		formatTable();
	}// function initComponents


    private void addHeaderClickListener(){
		// listener
		tableColumns.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int col = tableColumns.columnAtPoint(e.getPoint());
				if (col != LABEL_COL){
					proccesHeaderClick(col);
				}
			}
		});
	}

	private void proccesHeaderClick(int col){

		// get the first value
		Boolean firstValue = null;
		for(int row = 0;row < tableColumns.getRowCount();row++) {

			Boolean value = (Boolean)tableColumns.getModel().getValueAt(row, col);
			if (firstValue == null && value != null){
				firstValue = value;
			}

			if (firstValue != null) {
				tableColumns.setValueAt(!firstValue, row, col);
			}
		}
	}
	
	protected void formatTable() {
        tableColumns.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableColumns.getColumnModel().getColumn(LABEL_COL).setPreferredWidth(250);
        tableColumns.getColumnModel().getColumn(VISIBLE_COL).setPreferredWidth(50);
        tableColumns.getColumnModel().getColumn(RENDER_COL).setPreferredWidth(50);
        
    }

	protected void enableUpDown(boolean value) {
		btnUp.setEnabled(value);
		btnDown.setEnabled(value);
	}// function enableUpDown

	// move the selection on the table, -1 up, 0 down
	protected void moveSelection(int delta) {
		int pos = tableColumns.getSelectedRow();
		ColumnInfo ci = rows.remove(pos);
		pos += delta;
		rows.add(pos, ci);
		model.fireTableDataChanged();
		tableColumns.setRowSelectionInterval(pos, pos);
	}

	@Override
	public void handleActionPerformed(ActionEvent evt){		
		JButton btn = (JButton) evt.getSource();
		
		if (btn == btnUp && tableColumns.getSelectedRow() > 0)
			moveSelection(-1);
		else if (btn == btnDown && tableColumns.getSelectedRow() < rows.size() - 1)
			moveSelection(1);
	}// function actionPerformed

	class ColumnsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private String[] columns = { "Label", "Visible", "Render"};

		public ColumnsTableModel(int[] labels) {
			rows = new ArrayList<ColumnInfo>(labels.length);
			for (int i = 0; i < labels.length; ++i)
				rows.add(new ColumnInfo(labels[i]));
		}

		public ColumnsTableModel(List<ColumnInfo> labelsInfo) {
			int n = labelsInfo.size();
			rows = new ArrayList<ColumnInfo>(n);
			for (int i = 0; i < n; ++i)
				rows.add(labelsInfo.get(i).clone());
		}

		@Override
		public Class getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columns[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public int getRowCount() {
			return rows.size();
			// return frame.getParticlePicker().getFamilies().size();
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			try {
				if (column == RENDER_COL && !rows.get(row).allowRender)
					return false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			ColumnInfo col = rows.get(row);
			switch (column) {
			case LABEL_COL:
				col.labelName = ((String)value);
				break;
			case VISIBLE_COL:
				col.visible = (Boolean) value;
				break;
			case RENDER_COL:
				col.render = (Boolean) value;
				break;
			}

		}

		@Override
		public Object getValueAt(int row, int column) {
			ColumnInfo col = rows.get(row);
			switch (column) {
			case 0:
				return col.labelName;
			case 1:
				return col.visible;
			case 2:
				if (col.allowRender) {
					return col.render;
				}else {
					return null;
				}
			}
			return null;
		}

	}// class ColumnsTableModel

	// Custom renderer to render checkboxes only if value is not null
	public class RenderColumnRenderer extends JCheckBox implements TableCellRenderer {

		RenderColumnRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus, int row, int column) {

			if (isSelected) {
				setForeground(table.getSelectionForeground());
				//super.setBackground(table.getSelectionBackground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setSelected((value != null && ((Boolean) value).booleanValue()));

			// If the column allows renderization ...
			if (value == null){
				return null;
			} else {
				return this;
			}
		}

	}

}// class ColumnsJDialog
