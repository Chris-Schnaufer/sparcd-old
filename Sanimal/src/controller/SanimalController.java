package controller;

import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;

import model.ImageEntry;
import model.Location;
import model.SanimalData;
import model.Species;
import model.SpeciesEntry;
import view.SanimalInput;
import view.SanimalView;

/**
 * The main controller of sanimal in the MVC design pattern. It links the view to the model
 * 
 * @author David Slovikosky
 */
public class SanimalController
{
	// Declare the view and the model
	private final SanimalView sanimalView;
	private final SanimalData sanimalData;
	// Declare constants
	private static final long MILLIS_BETWEEN_CHECKS = 500;

	// DEBUG
	// Loc 1: 32.273302, -110.836417
	// Loc 2: 32.273057, -110.836507
	// Loc 3: 32.273390, -110.836450

	/**
	 * Constructor of the controller
	 * 
	 * @param sanimalView
	 *            The view to link the model to
	 * @param sanimalData
	 *            The model to link the view to
	 */
	public SanimalController(SanimalView sanimalView, SanimalData sanimalData)
	{
		this.sanimalView = sanimalView;
		this.sanimalData = sanimalData;

		///
		/// Begin to setup sanimalView with action listeners 
		///

		// When the user clicks a new image in the image list on the right
		sanimalView.addImageTreeValueChanged(event ->
		{
			// Update the selected item and update the timeline data's image list
			SanimalController.this.selectedItemUpdated();
			List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
			sanimalData.getTimelineData().updateList(selectedImages);
		});
		// When the user wants to load in images
		sanimalView.addImageBrowseListener(event ->
		{
			// Create a JFileChooser and load in images from the directory
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				sanimalData.getImageData().readAndAddImages(chooser.getSelectedFile(), sanimalView.searchSubdirectories());
				sanimalView.setImageList(sanimalData.getImageData().getHeadDirectory());
			}
		});
		// When the user selects a new location from the drop down
		sanimalView.addLocationSelectedListener(event ->
		{
			if (event.getStateChange() == ItemEvent.SELECTED)
			{
				Location selected = ((Location) event.getItem());
				if (selected != null)
					for (ImageEntry selectedImage : sanimalView.getSelectedImageEntries())
						selectedImage.setLocationTaken(selected);
				sanimalView.setLocation(selected);
				sanimalView.centerMapOn(selected.toGeoPosition());
			}
		});
		// When the user wants to add a new location
		sanimalView.addALToAddNewLocation(event ->
		{
			Location newLocation = SanimalInput.askUserForNewLocation();
			if (newLocation != null)
			{
				sanimalData.getLocationData().addLocation(newLocation);
				sanimalView.setLocationList(sanimalData.getLocationData().getRegisteredLocations());
			}
		});
		// When the user wants to add a new species
		sanimalView.addALToAddNewSpecies(event ->
		{
			Species species = SanimalInput.askUserForNewSpecies();
			if (species != null)
			{
				sanimalData.getSpeciesData().addSpecies(species);
				sanimalView.setSpeciesList(sanimalData.getSpeciesData().getRegisteredSpecies());
			}
		});
		// When the user wants to remove a location
		sanimalView.addALToRemoveLocation(event ->
		{
			if (sanimalView.getSelectedLocation() != null)
			{
				Location selected = sanimalView.getSelectedLocation();
				int numberOfSelectedOccourances = 0;
				// Count how many images have this location selected
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
					if (image.getLocationTaken() == selected)
						numberOfSelectedOccourances++;
				// If we have more than 1, ask the user if he'd like to continue
				if (numberOfSelectedOccourances >= 1)
					if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(sanimalView, "This location has already been set on multiple images, continue removing the location from all images?"))
						return;
				// Delete the location
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
					if (image.getLocationTaken() == selected)
						image.setLocationTaken(null);
				sanimalData.getLocationData().removeLocation(selected.toString());
				sanimalView.setLocationList(sanimalData.getLocationData().getRegisteredLocations());
				SanimalController.this.selectedItemUpdated();
			}
		});
		// When the user wants to remove a species
		sanimalView.addALToRemoveSpecies(event ->
		{
			if (sanimalView.getSelectedSpecies() != null)
			{
				Species selected = sanimalView.getSelectedSpecies();
				int numberOfSelectedOccourances = 0;
				// Count how many images have this species selected
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
					for (SpeciesEntry entry : image.getSpeciesPresent())
						if (entry.getSpecies() == selected)
							numberOfSelectedOccourances++;
				// If we have more than 1, ask the user if he'd like to continue
				if (numberOfSelectedOccourances >= 1)
					if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(sanimalView, "This species has already been set on multiple images, continue removing the species from all images?"))
						return;
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
				{
					Iterator<SpeciesEntry> iterator = image.getSpeciesPresent().iterator();
					while (iterator.hasNext())
						if (iterator.next().getSpecies() == selected)
							iterator.remove();
				}
				sanimalData.getSpeciesData().removeSpecies(selected);
				sanimalView.setSpeciesList(sanimalData.getSpeciesData().getRegisteredSpecies());
				SanimalController.this.selectedItemUpdated();
			}
		});
		// When the user adds a new species to the image
		sanimalView.addALToAddSpeciesToList(event ->
		{
			Species selectedSpecies = sanimalView.getSelectedSpecies();
			if (selectedSpecies != null)
			{
				// Ensure we have a species selected and an image selected
				if (!sanimalView.getSelectedImageEntries().isEmpty())
				{
					// Get the number of animals, and add it to the image
					Integer numberOfAnimals = Integer.MAX_VALUE;
					while (numberOfAnimals == Integer.MAX_VALUE)
					{
						try
						{
							String numberOfAnimalsString = JOptionPane.showInputDialog("How many animals of this species are in the image?");
							if (numberOfAnimalsString == null)
								return;
							numberOfAnimals = Integer.parseInt(numberOfAnimalsString);
						}
						catch (NumberFormatException exception)
						{
						}
					}
					List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
					for (ImageEntry imageEntry : selectedImages)
						imageEntry.addSpecies(selectedSpecies, numberOfAnimals);
					if (selectedImages.size() == 1)
						sanimalView.setSpeciesEntryList(selectedImages.get(0).getSpeciesPresent());
				}
				else
				{
					JOptionPane.showConfirmDialog(sanimalView, "You must select an image(s) to apply this species to!", "Error adding species to Image", JOptionPane.DEFAULT_OPTION);
				}
			}
			else
			{
				JOptionPane.showConfirmDialog(sanimalView, "You must select a species from the drop down first!", "Error adding species to Image", JOptionPane.DEFAULT_OPTION);
			}
			SanimalController.this.selectedItemUpdated();
		});
		// When the user removes an animal from an image
		sanimalView.addALToRemoveSpeciesFromList(event ->
		{
			Species selectedSpecies = sanimalView.getSelectedSpecies();
			List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
			if (selectedSpecies != null)
				for (ImageEntry imageEntry : selectedImages)
					imageEntry.removeSpecies(selectedSpecies);
			SanimalController.this.selectedItemUpdated();
		});
		// When the user clicks "Perform Analysis"
		sanimalView.addALToPerformAnalysis(event ->
		{
			Integer eventInterval = sanimalView.getAnalysisEventInterval();
			if (eventInterval != -1)
				sanimalView.setOutputText(sanimalData.getOutputFormatter().format(sanimalView.getSelectedImageEntries(), eventInterval));
		});
		// When the user clicks to create an excel file
		sanimalView.addALToCreateExcel(event ->
		{
			// Create a JFileChooser, and then create the excel file
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select the location to save the excel file to");
			chooser.setFileFilter(new FileNameExtensionFilter("Excel files (.xlsx)", "xlsx"));
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			chooser.setSelectedFile(new File("Sanimal.xlsx"));
			int response = chooser.showSaveDialog(sanimalView);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				File directory = chooser.getSelectedFile();
				if (sanimalData.getExcelFormatter().format(directory, sanimalView.getSelectedImageEntries(), sanimalView.getSelectedDataTypeRadioButton(), sanimalView.getAnalysisEventInterval()))
					JOptionPane.showMessageDialog(sanimalView, "Excel file saved sucessfully!");
				else
					JOptionPane.showMessageDialog(sanimalView, "There was an error when saving the excel file.");
			}
		});
		// When the user selects the timeline
		sanimalView.addALToPrgDataShow(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent event)
			{
				double percentage = (double) event.getX() / (double) event.getComponent().getWidth();
				if (percentage < 0)
					percentage = 0;
				if (percentage > 1)
					percentage = 1;
				List<ImageEntry> images = sanimalData.getTimelineData().imageListByPercent(percentage, DateUtils.MILLIS_PER_DAY / 2);
				sanimalView.setImagesDrawnOnMap(images);
			}

			@Override
			public void mousePressed(MouseEvent event)
			{
			}

			@Override
			public void mouseExited(MouseEvent event)
			{
			}

			@Override
			public void mouseEntered(MouseEvent event)
			{
			}

			@Override
			public void mouseClicked(MouseEvent event)
			{
			}
		});
		// When the user drags the timeline
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		sanimalView.addALToPrgDataShow(new MouseMotionListener()
		{
			@Override
			public void mouseMoved(MouseEvent event)
			{
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				// Only perform the update once every .5 seconds. This ensures that this update does not get
				// spammed and lag the program
				if (stopWatch.getTime() > MILLIS_BETWEEN_CHECKS)
				{
					double percentage = (double) event.getX() / (double) event.getComponent().getWidth();
					if (percentage < 0)
						percentage = 0;
					if (percentage > 1)
						percentage = 1;
					List<ImageEntry> images = sanimalData.getTimelineData().imageListByPercent(percentage, DateUtils.MILLIS_PER_DAY / 2);
					sanimalView.setImagesDrawnOnMap(images);
					stopWatch.reset();
					stopWatch.start();
				}
			}
		});
		// When the user creates "All Pictures" output
		sanimalView.addALToAllPictures(event ->
		{
			Integer eventInterval = sanimalView.getAnalysisEventInterval();
			if (eventInterval != -1)
				sanimalView.setOutputText(sanimalData.getOutputFormatter().createAllPictures(sanimalView.getSelectedImageEntries(), eventInterval));
		});

		// Set the view to visible now that it has been constructed
		sanimalView.setVisible(true);

		///
		/// End setup sanimalView with action listeners 
		///
	}

	/**
	 * When the selected item is updated, we need to go through each entry and find common features between them to be displayed.
	 */
	public void selectedItemUpdated()
	{
		// The first entry to compare to
		ImageEntry first = null;
		// The first image's location
		Location firstLocation = null;
		// The first image's date
		String firstDate = null;
		// The first image's species
		List<SpeciesEntry> firstSpeciesEntries = null;
		List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
		// Find similarities
		for (ImageEntry current : selectedImages)
		{
			// Setup the first entry
			if (first == null)
			{
				first = current;
				firstLocation = first.getLocationTaken();
				firstDate = first.getDateTakenFormatted();
				firstSpeciesEntries = first.getSpeciesPresent();
				Collections.sort(firstSpeciesEntries);
				continue;
			}
			// If the first date is not null yet, we check to see if the current date equals the first date. If so,
			// Continue, else we set the first date to null
			if (firstDate != null)
				if (!current.getDateTakenFormatted().equals(firstDate))
					firstDate = null;
			// Perform the same operation on location as above
			if (firstLocation != null)
				if (current.getLocationTaken() != firstLocation)
					firstLocation = null;
			// Perform the same operation on species as above
			if (firstSpeciesEntries != null)
			{
				Collections.sort(current.getSpeciesPresent());
				if (!current.getSpeciesPresent().equals(firstSpeciesEntries))
					firstSpeciesEntries = null;
			}
		}
		// Update the fields accordingly
		sanimalView.setLocation(firstLocation);
		sanimalView.setDate(firstDate);
		sanimalView.setSpeciesEntryList(firstSpeciesEntries);
		sanimalView.refreshLocationFields();
		if (selectedImages.size() == 1)
			sanimalView.setThumbnailImage(first);
		else
			sanimalView.setThumbnailImage(null);
	}
}
