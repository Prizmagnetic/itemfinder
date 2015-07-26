package de.yogularm.minecraft.itemfinder.gui;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;

import de.yogularm.minecraft.itemfinder.region.DroppedItem;

public class ItemList {
	private Component component;
	private JXTable table;
	private ItemTableModel model;
	private TableRowSorter<ItemTableModel> sorter;
	private JPopupMenu contextMenu;

	public ItemList() {
		initUI();
	}

	private void initUI() {
		model = new ItemTableModel();
		table = new JXTable(model);
		sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);
		sorter.setComparator(0, new NaturalOrderComparator()); // for item name
		sorter.setSortKeys(Arrays.asList(
				new RowSorter.SortKey(4, SortOrder.DESCENDING),
				new RowSorter.SortKey(3, SortOrder.ASCENDING)));
		
		table.getColumnModel().getColumn(0).setWidth(200);

		component = new JScrollPane(table);

		contextMenu = new JPopupMenu();
		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent actionEvent) {
				Object text = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
				if (text == null) {
					return;
				}
				Toolkit.getDefaultToolkit().getSystemClipboard()
						.setContents(new StringSelection(text.toString()), null);
			}
		});
	 	contextMenu.add(copyItem);

		// manually show the popup menu so that the cell under the mouse is selected first
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// Left mouse click
				if (SwingUtilities.isLeftMouseButton(e)) {
					// Do something
				}
				// Right mouse click
				else if (SwingUtilities.isRightMouseButton(e)) {
					// get the coordinates of the mouse click
					Point p = e.getPoint();

					// get the row index that contains that coordinate
					int rowNumber = table.rowAtPoint(p);

					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = table.getSelectionModel();

					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval(rowNumber, rowNumber);

					int colNumber = table.columnAtPoint(p);
					table.getColumnModel().getSelectionModel().setSelectionInterval(colNumber, colNumber);

					contextMenu.show(table, p.x, p.y);
				}
			}
		});

	}

	public void setItems(List<DroppedItem> items) {
		model.setItems(items);
		table.packAll(); // adjust column widths
	}

	public Component getComponent() {
		return component;
	}

	private static class ItemTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1904800093497922295L;

		private static final String[] COLUMNS = new String[] { "Item Name",
				"Stack Size", "Location", "Age", "Chunk Update" };

		private List<DroppedItem> items;

		public ItemTableModel() {
			this.items = new ArrayList<>();
		}

		public void setItems(List<DroppedItem> items) {
			this.items = items;
			fireTableDataChanged();
		}

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMNS[column];
		}

		@Override
		public int getRowCount() {
			return items.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= items.size())
				return "";
			DroppedItem item = items.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return item.getName();
			case 1:
				return item.getCount();
			case 2:
				return item.getPosition().toRoundedString();
			case 3:
				// there are items with age 0 which should be displayed at the bottom
				// HourSecond will render Integer.MAX_VALUE as --
				return new HourSecond(item.getAge() == 0 ? Integer.MAX_VALUE : item.getAge() / 20);
			case 4:
				return new RelativeTime(- item.getRelativeChunkUpdateTime());
			default:
				return "";
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
				return Integer.class;
			case 2:
				return String.class;
			case 3:
				return HourSecond.class;
			case 4:
				return RelativeTime.class;
			default:
				return null;
			}
		}
	}
}
