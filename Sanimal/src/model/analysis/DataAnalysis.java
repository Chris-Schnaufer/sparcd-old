package model.analysis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import library.MoonCalculator;
import model.ImageEntry;
import model.Location;
import model.Species;
import model.SpeciesEntry;

/**
 * A class used to collect all of the basic data in the analysis. Also contains utility methods for performing various analysis.
 * 
 * @author David Slovikosky
 */
public class DataAnalysis
{
	// Null locations means that at least 1 image has no location tag
	private boolean nullLocationsFound = false;
	// List of all images, species, and years found over the image list
	private List<Location> allImageLocations = new ArrayList<Location>();
	private List<Species> allImageSpecies = new ArrayList<Species>();
	private List<Integer> allImageYears = new ArrayList<Integer>();
	// A list of all images but sorted by date instead of randomly
	private List<ImageEntry> imagesSortedByDate;
	// The event interval, in minutes
	private Integer eventInterval = 60;
	// A pre-calculated list of all full and new moons over the image's interval
	private List<Date> fullMoons = new ArrayList<Date>();
	private List<Date> newMoons = new ArrayList<Date>();

	/**
	 * Constructor for the analysis
	 * 
	 * @param images
	 *            A list of images to perform the analysis on
	 * @param eventInterval
	 *            The event interval given in minutes
	 */
	public DataAnalysis(List<ImageEntry> images, Integer eventInterval)
	{
		this.eventInterval = eventInterval;
		// Find all image locations
		for (ImageEntry entry : images)
			if (entry.getLocationTaken() != null)
			{
				if (!allImageLocations.contains(entry.getLocationTaken()))
					allImageLocations.add(entry.getLocationTaken());
			}
			else
				nullLocationsFound = true;

		// Sort the locations by name
		Collections.sort(allImageLocations, (loc1, loc2) ->
		{
			return loc1.getName().compareTo(loc2.getName());
		});

		// Find all image species 
		for (ImageEntry imageEntry : images)
			for (SpeciesEntry speciesEntry : imageEntry.getSpeciesPresent())
				if (!allImageSpecies.contains(speciesEntry.getSpecies()))
					allImageSpecies.add(speciesEntry.getSpecies());

		// Sort species by name
		Collections.sort(allImageSpecies, (species1, species2) ->
		{
			return species1.getName().compareTo(species2.getName());
		});

		// Find all image years
		for (ImageEntry imageEntry : images)
		{
			Integer year = DateUtils.toCalendar(imageEntry.getDateTaken()).get(Calendar.YEAR);
			if (!allImageYears.contains(year))
				allImageYears.add(year);
		}

		// Sort years first to last
		Collections.sort(allImageYears);

		// Create a copy of "images", and sort it by date
		imagesSortedByDate = new ArrayList<ImageEntry>(images);
		Collections.sort(imagesSortedByDate, (entry1, entry2) ->
		{
			return entry1.getDateTaken().compareTo(entry2.getDateTaken());
		});

		// If we have at least one image, begin calculating lunar cycles
		if (imagesSortedByDate.size() > 0)
		{
			// Full moon calculations
			Date first = imagesSortedByDate.get(0).getDateTaken();
			Date last = imagesSortedByDate.get(imagesSortedByDate.size() - 1).getDateTaken();
			while (first.before(last))
			{
				double julianDate = MoonCalculator.getJulian(first);
				double[] phases = MoonCalculator.getPhase(julianDate);
				double fullMoon = MoonCalculator.getLunation(julianDate, phases[MoonCalculator.MOONPHASE], 180);
				long fullMillis = MoonCalculator.toMillisFromJulian(fullMoon);
				Date nextFullMoonDate = new Date(fullMillis);
				fullMoons.add(nextFullMoonDate);
				first = new Date(nextFullMoonDate.getTime() + 20 * 1000 * 60 * 60 * 24);
			}

			// New moon calculations
			Date first2 = imagesSortedByDate.get(0).getDateTaken();
			Date last2 = imagesSortedByDate.get(imagesSortedByDate.size() - 1).getDateTaken();
			while (first2.before(last2))
			{
				double julianDate = MoonCalculator.getJulian(first2);
				double[] phases = MoonCalculator.getPhase(julianDate);
				double newMoon = MoonCalculator.getLunation(julianDate, phases[MoonCalculator.MOONPHASE], 0);
				long newMillis = MoonCalculator.toMillisFromJulian(newMoon);
				Date nextNewMoonDate = new Date(newMillis);
				newMoons.add(nextNewMoonDate);
				first2 = new Date(nextNewMoonDate.getTime() + 20 * 1000 * 60 * 60 * 24);
			}
		}
	}

	/**
	 * Returns the first image entry based on date taken
	 * 
	 * @param images
	 *            The list of images to search through
	 * @return The image entry that is first in the list. Returns null if no images are present
	 */
	public ImageEntry getFirstImageInList(List<ImageEntry> images)
	{
		ImageEntry first = images.size() == 0 ? null : images.get(0);
		for (ImageEntry imageEntry : images)
			if (imageEntry.getDateTaken().before(first.getDateTaken()))
				first = imageEntry;
		return first;
	}

	/**
	 * Returns the last image entry based on date taken
	 * 
	 * @param images
	 *            The list of images to search through
	 * @return The image entry that is last in the list. Returns null if no images are present
	 */
	public ImageEntry getLastImageInList(List<ImageEntry> images)
	{
		ImageEntry last = images.size() == 0 ? null : images.get(0);
		for (ImageEntry imageEntry : images)
			if (imageEntry.getDateTaken().after(last.getDateTaken()))
				last = imageEntry;
		return last;
	}

	/**
	 * Returns a list of locations that the image list contains
	 * 
	 * @param images
	 *            The list of images to search through
	 * @return A list containing each location found in the image list
	 */
	public List<Location> locationsForImageList(List<ImageEntry> images)
	{
		List<Location> locations = new ArrayList<Location>();

		for (ImageEntry image : images)
			if (image.getLocationTaken() != null)
				if (!locations.contains(image.getLocationTaken()))
					locations.add(image.getLocationTaken());

		Collections.sort(locations, (loc1, loc2) ->
		{
			return loc1.getName().compareTo(loc2.getName());
		});

		return locations;
	}

	/**
	 * Get the activity for a list of images. Images MUST first be filtered by location and species to achieve a total accumulation
	 * 
	 * @param images
	 *            The list of images to test
	 * @return The activity for the image list
	 */
	public Integer activityForImageList(List<ImageEntry> images)
	{
		Integer activity = 0;

		int oldHour = -1;
		int oldDay = -1;
		int oldYear = -1;
		for (ImageEntry image : images)
		{
			Calendar calendar = DateUtils.toCalendar(image.getDateTaken());
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int day = calendar.get(Calendar.DAY_OF_YEAR);
			int year = calendar.get(Calendar.YEAR);
			// If either the hour, day, or year changes, we're onto a new activity
			if ((hour != oldHour) || (oldDay != day) || (oldYear != year))
			{
				activity = activity + 1;
				oldHour = hour;
				oldDay = day;
				oldYear = year;
			}
		}

		return activity;
	}

	/**
	 * Get the period for a list of images. Images MUST first be filtered by location and species to achieve a total accumulation
	 * 
	 * @param images
	 *            The list of images to test. This list MUST be sorted by date for proper results
	 * @return The period for the image list
	 */
	public Integer periodForImageList(List<ImageEntry> images)
	{
		Integer period = 0;

		long lastImageTimeMillis = 0;
		for (ImageEntry image : images)
		{
			long imageTimeMillis = image.getDateTaken().getTime();
			long differenceMillis = imageTimeMillis - lastImageTimeMillis;
			long differenceMinutes = differenceMillis / 1000 / 60;
			// If the difference between the last image and the current one must be > the event interval
			if (differenceMinutes >= eventInterval)
			{
				period++;
			}
			lastImageTimeMillis = imageTimeMillis;
		}

		return period;
	}

	/**
	 * Get the abundance value for a list of images. Images MUST first be filtered by location and species to achieve a total accumulation
	 * 
	 * @param images
	 *            The list of images to search through (must be sorted)
	 * @param speciesFilter
	 *            The species for which to look for. May be null for any species
	 * @return The abundance value for "images"
	 */
	public Integer abundanceForImageList(List<ImageEntry> images, Species speciesFilter)
	{
		Integer abundance = 0;

		long lastImageTimeMillis = 0;
		Integer maxAnimalsInEvent = 0;
		for (ImageEntry image : images)
		{
			long imageTimeMillis = image.getDateTaken().getTime();
			long differenceMillis = imageTimeMillis - lastImageTimeMillis;
			long differenceMinutes = differenceMillis / 1000 / 60;

			// If the current image is further away than the event interval, add the current max number of animals to the total
			if (differenceMinutes >= eventInterval)
			{
				abundance = abundance + maxAnimalsInEvent;
				maxAnimalsInEvent = 0;
			}

			// The max number of animals is the max number of animals in this image or the max number of animals in the last image
			for (SpeciesEntry speciesEntry : image.getSpeciesPresent())
				if (speciesFilter == null || speciesEntry.getSpecies() == speciesFilter)
					maxAnimalsInEvent = Math.max(maxAnimalsInEvent, speciesEntry.getAmount());

			lastImageTimeMillis = imageTimeMillis;
		}

		abundance = abundance + maxAnimalsInEvent;

		return abundance;
	}

	/**
	 * @return A list containing all image locations
	 */
	public List<Location> getAllImageLocations()
	{
		return allImageLocations;
	}

	/**
	 * @return True if at least one image had a null location, false if not
	 */
	public boolean nullLocationsFound()
	{
		return nullLocationsFound;
	}

	/**
	 * @return A list containing all image species
	 */
	public List<Species> getAllImageSpecies()
	{
		return allImageSpecies;
	}

	/**
	 * @return A list containing all image years
	 */
	public List<Integer> getAllImageYears()
	{
		return allImageYears;
	}

	/**
	 * @return A list containing all image sorted by date
	 */
	public List<ImageEntry> getImagesSortedByDate()
	{
		return imagesSortedByDate;
	}

	/**
	 * @return A list containing all full moon dates
	 */
	public List<Date> getFullMoons()
	{
		return fullMoons;
	}

	/**
	 * @return A list containing all new moon dates
	 */
	public List<Date> getNewMoons()
	{
		return newMoons;
	}
}
