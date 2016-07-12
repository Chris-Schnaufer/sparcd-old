/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;

import library.comboBox.ComboBoxFullMenu;
import model.ImageDirectory;
import model.ImageEntry;
import model.Location;
import model.Species;
import model.SpeciesEntry;

public class ImageImportView extends JFrame
{
	private JTree treImages;
	private JTextField txtDate;
	private JLabel lblDate;
	private JLabel lblThumbnail;
	private JPanel pnlImageBrowser;
	private JCheckBox chxIncludeSubdirectories;
	private JButton btnBrowseForImages;
	private JScrollPane pneImageList;
	private JPanel pnlPropertyList;
	private JLabel lblLocation;
	private ComboBoxFullMenu<Location> cbxLocation;
	private JButton btnAddNewLocation;
	private JButton btnRemoveLocation;
	private JLabel lblLocationLat;
	private JTextField txtLat;
	private JLabel lblLocationLng;
	private JTextField txtLng;
	private JLabel lblLocationElevation;
	private JTextField txtElevation;
	private JLabel lblSpecies;
	private ComboBoxFullMenu<Species> cbxSpecies;
	private JButton btnAddNewSpecies;
	private JButton btnRemoveSpecies;
	private JButton btnAddSpeciesToList;
	private JButton btnRemoveSpeciesFromList;
	private JButton btnFinishImporting;
	private JPanel pnlSpeciesPresent;
	private JLabel lblSpeciesEntries;
	private JScrollPane pneSpeciesList;
	private JList lstSpecies;
	private JPanel pnlMap;
	private JXMapViewer mapViewer;

	public ImageImportView()
	{
		this.getContentPane().setLayout(null);
		//this.setResizable(false);
		this.setTitle("Image Importer");
		this.setSize(1504, 637);

		lblThumbnail = new JLabel();
		lblThumbnail.setBounds(298, 11, 424, 362);
		lblThumbnail.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(lblThumbnail);

		pnlPropertyList = new JPanel();
		pnlPropertyList.setLayout(null);
		pnlPropertyList.setBounds(10, 384, 462, 202);
		pnlPropertyList.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlPropertyList);

		lblDate = new JLabel("Date Taken:");
		lblDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDate.setBounds(10, 14, 87, 14);
		pnlPropertyList.add(lblDate);

		txtDate = new JTextField();
		txtDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtDate.setBounds(107, 11, 340, 20);
		txtDate.setEditable(false);
		pnlPropertyList.add(txtDate);

		lblLocation = new JLabel("Location: ");
		lblLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocation.setBounds(10, 39, 87, 14);
		pnlPropertyList.add(lblLocation);

		cbxLocation = new ComboBoxFullMenu<Location>();
		cbxLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxLocation.setBounds(107, 36, 167, 23);
		cbxLocation.setSelectedIndex(-1);
		pnlPropertyList.add(cbxLocation);

		btnAddNewLocation = new JButton("Add");
		btnAddNewLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddNewLocation.setBounds(284, 35, 70, 23);
		pnlPropertyList.add(btnAddNewLocation);

		btnRemoveLocation = new JButton("Remove");
		btnRemoveLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveLocation.setBounds(360, 36, 87, 23);
		pnlPropertyList.add(btnRemoveLocation);

		lblLocationLat = new JLabel("Latitude: ");
		lblLocationLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationLat.setBounds(10, 64, 87, 14);
		pnlPropertyList.add(lblLocationLat);

		txtLat = new JTextField();
		txtLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLat.setBounds(107, 61, 340, 20);
		txtLat.setEditable(false);
		pnlPropertyList.add(txtLat);

		lblLocationLng = new JLabel("Longitude: ");
		lblLocationLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationLng.setBounds(10, 89, 87, 14);
		pnlPropertyList.add(lblLocationLng);

		txtLng = new JTextField();
		txtLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLng.setBounds(107, 86, 340, 20);
		txtLng.setEditable(false);
		pnlPropertyList.add(txtLng);

		lblLocationElevation = new JLabel("Elevation: ");
		lblLocationElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationElevation.setBounds(10, 114, 87, 14);
		pnlPropertyList.add(lblLocationElevation);

		txtElevation = new JTextField();
		txtElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtElevation.setBounds(107, 111, 340, 20);
		txtElevation.setEditable(false);
		pnlPropertyList.add(txtElevation);

		lblSpecies = new JLabel("Species: ");
		lblSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSpecies.setBounds(10, 139, 87, 14);
		pnlPropertyList.add(lblSpecies);

		cbxSpecies = new ComboBoxFullMenu<Species>();
		cbxSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxSpecies.setBounds(107, 135, 167, 23);
		cbxSpecies.setSelectedIndex(-1);
		pnlPropertyList.add(cbxSpecies);

		btnAddNewSpecies = new JButton("Add");
		btnAddNewSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddNewSpecies.setBounds(284, 135, 70, 23);
		btnAddNewSpecies.setToolTipText("Add a new species to the species dictionary");
		pnlPropertyList.add(btnAddNewSpecies);

		btnRemoveSpecies = new JButton("Remove");
		btnRemoveSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveSpecies.setBounds(360, 135, 87, 23);
		btnRemoveSpecies.setToolTipText("Remove the selected species from the species dictionary");
		pnlPropertyList.add(btnRemoveSpecies);

		btnAddSpeciesToList = new JButton("Add Species");
		btnAddSpeciesToList.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddSpeciesToList.setBounds(180, 164, 115, 23);
		btnAddSpeciesToList.setToolTipText("Add the selected species to the selected image");
		pnlPropertyList.add(btnAddSpeciesToList);

		btnRemoveSpeciesFromList = new JButton("Remove Species");
		btnRemoveSpeciesFromList.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveSpeciesFromList.setBounds(305, 164, 142, 23);
		btnRemoveSpeciesFromList.setToolTipText("Remove the selected species from the selected image");
		pnlPropertyList.add(btnRemoveSpeciesFromList);

		btnFinishImporting = new JButton("Finish Importing");
		btnFinishImporting.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnFinishImporting.setBounds(10, 164, 160, 23);
		pnlPropertyList.add(btnFinishImporting);

		pnlSpeciesPresent = new JPanel();
		pnlSpeciesPresent.setLayout(null);
		pnlSpeciesPresent.setBounds(482, 384, 240, 202);
		pnlSpeciesPresent.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlSpeciesPresent);

		lblSpeciesEntries = new JLabel("Species in image:");
		lblSpeciesEntries.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSpeciesEntries.setBounds(10, 10, 220, 14);
		pnlSpeciesPresent.add(lblSpeciesEntries);

		pneSpeciesList = new JScrollPane();
		lstSpecies = new JList();
		lstSpecies.setModel(new DefaultListModel());
		pneSpeciesList.setBounds(10, 35, 220, 156);
		pneSpeciesList.setViewportView(lstSpecies);
		pnlSpeciesPresent.add(pneSpeciesList);

		pnlImageBrowser = new JPanel();
		pnlImageBrowser.setBounds(10, 11, 278, 362);
		getContentPane().add(pnlImageBrowser);
		pnlImageBrowser.setBorder(new LineBorder(Color.BLACK));
		pnlImageBrowser.setLayout(null);

		chxIncludeSubdirectories = new JCheckBox("Include Subdirectories");
		chxIncludeSubdirectories.setSelected(true);
		chxIncludeSubdirectories.setBounds(137, 332, 131, 23);
		pnlImageBrowser.add(chxIncludeSubdirectories);

		btnBrowseForImages = new JButton("Select Images");
		btnBrowseForImages.setBounds(6, 332, 125, 23);
		pnlImageBrowser.add(btnBrowseForImages);

		pneImageList = new JScrollPane();
		treImages = new JTree((TreeModel) null);
		pneImageList.setBounds(10, 11, 258, 314);
		pneImageList.setViewportView(treImages);
		pnlImageBrowser.add(pneImageList);

		pnlMap = new JPanel();
		pnlMap.setBounds(732, 11, 746, 575);
		getContentPane().add(pnlMap);
		pnlMap.setBorder(new LineBorder(Color.BLACK));
		pnlMap.setLayout(null);

		TileFactoryInfo tileFactoryInfo = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID);
		DefaultTileFactory tileFactory = new DefaultTileFactory(tileFactoryInfo);
		tileFactory.setThreadPoolSize(8);
		// ADDED
		File cacheDir = new File(System.getProperty("user.home") + File.separator + "Sanimal Map Tiles");
		LocalResponseCache.installResponseCache(tileFactoryInfo.getBaseURL(), cacheDir, false);

		mapViewer = new JXMapViewer();
		mapViewer.setLayout(null);
		mapViewer.setBounds(0, 0, 746, 575);
		mapViewer.setTileFactory(tileFactory);

		GeoPosition tucson = new GeoPosition(32.2217, -110.9265);
		mapViewer.setZoom(10);
		mapViewer.setAddressLocation(tucson);

		MouseInputListener listener = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(listener);
		mapViewer.addMouseMotionListener(listener);
		mapViewer.addMouseListener(new CenterMapListener(mapViewer));
		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
		mapViewer.addKeyListener(new PanKeyListener(mapViewer));

		pnlMap.add(mapViewer);

		this.setLocationRelativeTo(null);
	}

	public void addImageTreeValueChanged(TreeSelectionListener listener)
	{
		this.treImages.addTreeSelectionListener(listener);
	}

	public void addImageBrowseListener(ActionListener listener)
	{
		this.btnBrowseForImages.addActionListener(listener);
	}

	public void addLocationSelectedListener(ItemListener listener)
	{
		this.cbxLocation.addItemListener(listener);
	}

	public void addALToAddNewLocation(ActionListener listener)
	{
		this.btnAddNewLocation.addActionListener(listener);
	}

	public void addALToAddNewSpecies(ActionListener listener)
	{
		this.btnAddNewSpecies.addActionListener(listener);
	}

	public void addALToRemoveLocation(ActionListener listener)
	{
		this.btnRemoveLocation.addActionListener(listener);
	}

	public void addALToRemoveSpecies(ActionListener listener)
	{
		this.btnRemoveSpecies.addActionListener(listener);
	}

	public void addALToAddSpeciesToList(ActionListener listener)
	{
		this.btnAddSpeciesToList.addActionListener(listener);
	}

	public void addALToFinishImporting(ActionListener listener)
	{
		this.btnFinishImporting.addActionListener(listener);
	}

	public void addALToRemoveSpeciesFromList(ActionListener listener)
	{
		this.btnRemoveSpeciesFromList.addActionListener(listener);
	}

	public Location getSelectedLocation()
	{
		if (this.cbxLocation.getSelectedIndex() == -1)
			return null;
		else
			return (Location) this.cbxLocation.getSelectedItem();
	}

	public Species getSelectedSpecies()
	{
		if (this.cbxSpecies.getSelectedIndex() == -1)
			return null;
		else
			return (Species) this.cbxSpecies.getSelectedItem();
	}

	public boolean searchSubdirectories()
	{
		return this.chxIncludeSubdirectories.isSelected();
	}

	public void setThumbnailImage(ImageEntry image)
	{
		if (image != null)
			this.lblThumbnail.setIcon(image.createIcon(this.lblThumbnail.getWidth(), this.lblThumbnail.getHeight()));
		else
			this.lblThumbnail.setIcon(null);
	}

	public void setDate(String date)
	{
		this.txtDate.setText(date);
	}

	public void setLocation(Location imageLoc)
	{
		if (imageLoc != null)
			this.cbxLocation.setSelectedItem(imageLoc);
		else
			this.cbxLocation.setSelectedIndex(-1);
		this.refreshLocationFields();
	}

	public void setSpecies(Species species)
	{
		if (species != null)
			this.cbxSpecies.setSelectedItem(species);
		else
			this.cbxSpecies.setSelectedIndex(-1);
	}

	public void setLocationList(List<Location> locations)
	{
		if (!locations.contains((Location) this.cbxLocation.getSelectedItem()))
			this.cbxLocation.setSelectedIndex(-1);
		this.cbxLocation.removeAllItems();
		for (Location location : locations)
			this.cbxLocation.addItem(location);
	}

	public void setSpeciesList(List<Species> species)
	{
		Species selectedSpecies = (Species) this.cbxSpecies.getSelectedItem();
		this.cbxSpecies.removeAllItems();
		for (Species species2 : species)
			this.cbxSpecies.addItem(species2);
		if (species.contains(selectedSpecies))
			this.cbxSpecies.setSelectedItem(selectedSpecies);
	}

	public void setSpeciesEntryList(List<SpeciesEntry> speciesEntries)
	{
		if (this.lstSpecies.getModel() instanceof DefaultListModel<?>)
		{
			DefaultListModel<SpeciesEntry> items = (DefaultListModel<SpeciesEntry>) this.lstSpecies.getModel();
			items.clear();
			if (speciesEntries != null)
				for (SpeciesEntry entry : speciesEntries)
					items.addElement(entry);
		}
	}

	public void setImageList(ImageDirectory imageDirectory)
	{
		DefaultMutableTreeNode head = new DefaultMutableTreeNode(imageDirectory);
		this.createTreeFromImageDirectory(head, imageDirectory);
		this.treImages.setModel(new DefaultTreeModel(head));
	}

	private void createTreeFromImageDirectory(DefaultMutableTreeNode headNode, ImageDirectory headDirectory)
	{
		for (ImageEntry image : headDirectory.getImages())
			headNode.add(new DefaultMutableTreeNode(image));
		for (ImageDirectory subDirectory : headDirectory.getSubDirectories())
		{
			DefaultMutableTreeNode subDirectoryNode = new DefaultMutableTreeNode(subDirectory);
			this.createTreeFromImageDirectory(subDirectoryNode, subDirectory);
			headNode.add(subDirectoryNode);
		}
	}

	public List<ImageEntry> getSelectedImageEntries()
	{
		List<ImageEntry> entries = new ArrayList<ImageEntry>();
		for (TreePath path : this.treImages.getSelectionModel().getSelectionPaths())
		{
			Object selectedElement = path.getLastPathComponent();
			if (selectedElement instanceof DefaultMutableTreeNode)
			{
				Object selectedObject = ((DefaultMutableTreeNode) selectedElement).getUserObject();
				if (selectedObject instanceof ImageEntry)
				{
					ImageEntry current = (ImageEntry) selectedObject;
					entries.add(current);
				}
				if (selectedObject instanceof ImageDirectory)
				{
					ImageDirectory current = (ImageDirectory) selectedObject;
					this.addAllImagesUnderDirectory(current, entries);
				}
			}
		}
		return entries;
	}

	private void addAllImagesUnderDirectory(ImageDirectory directory, List<ImageEntry> toAddTo)
	{
		for (ImageEntry entry : directory.getImages())
			if (!toAddTo.contains(entry))
				toAddTo.add(entry);
		for (ImageDirectory subDirectory : directory.getSubDirectories())
			this.addAllImagesUnderDirectory(subDirectory, toAddTo);
	}

	public List<ImageEntry> getAllTreeImageEntries()
	{
		List<ImageEntry> entries = new ArrayList<ImageEntry>();
		if (this.treImages.getModel().getRoot() instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.treImages.getModel().getRoot();
			for (DefaultMutableTreeNode node : Collections.<DefaultMutableTreeNode> list(root.preorderEnumeration()))
				if (node.getUserObject() instanceof ImageEntry)
					entries.add((ImageEntry) node.getUserObject());
		}
		return entries;
	}

	public void refreshLocationFields()
	{
		Location currentlySelected = ((Location) cbxLocation.getSelectedItem());
		if (currentlySelected != null)
		{
			txtLat.setText(Double.toString(currentlySelected.getLat()));
			txtLng.setText(Double.toString(currentlySelected.getLng()));
			txtElevation.setText(Double.toString(currentlySelected.getElevation()));
		}
		else
		{
			txtLat.setText("");
			txtLng.setText("");
			txtElevation.setText("");
		}
	}

	public Species askUserForNewSpecies()
	{
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new species");
			if (name == null)
				return null;
		}
		return new Species(name);
	}

	public Location askUserForNewLocation()
	{
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new location");
			if (name == null)
				return null;
		}
		Double latitude = Double.MAX_VALUE;
		while (latitude == Double.MAX_VALUE)
		{
			try
			{
				String latitudeString = JOptionPane.showInputDialog("Enter the latitude of location '" + name + "'");
				if (latitudeString == null)
					return null;
				latitude = Double.parseDouble(latitudeString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		Double longitude = Double.MAX_VALUE;
		while (longitude == Double.MAX_VALUE)
		{
			try
			{
				String longitudeString = JOptionPane.showInputDialog("Enter the longitude of location '" + name + "'");
				if (longitudeString == null)
					return null;
				longitude = Double.parseDouble(longitudeString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		Double elevation = Double.MAX_VALUE;
		while (elevation == Double.MAX_VALUE)
		{
			try
			{
				String elevationString = JOptionPane.showInputDialog("Enter the elevation of location '" + name + "'");
				if (elevationString == null)
					return null;
				elevation = Double.parseDouble(elevationString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		return new Location(name, latitude, longitude, elevation);
	}
}
