package model.analysis.textFormatters;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;

/**
 * The text formatter for species activity patterns
 * 
 * @author David Slovikosky
 */
public class ActivityPatternFormatter extends TextFormatter
{
	public ActivityPatternFormatter(List<ImageEntry> images, DataAnalysis analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each species daily activity patterns are given for all species by one hour segments. The species is listed and in parentheses (the number
	 * of reords used in the activity calculation / the total number of records some of which might be sequentil). The first column, labeled Hour,
	 * shows the hour segments starting and ending at midnight. Activity pattern is given by the number of records collected from all locations
	 * analyzed for all years, and in frequency for all years and months, and for all years and each month (since activity can vary by month). The
	 * total number of records for all years that was used is also given. The number of records matches the number of pictures listed under NUMBER OF
	 * PICTURES AND FILTERED PICTURES PER YEAR above.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printActivityPatterns()
	{
		String toReturn = "";

		toReturn = toReturn + "ACTIVITY PATTERNS\n";
		toReturn = toReturn + " Activity in one-hour segments - Species (Number of pictures in one hour segments/Total number of pics)\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			String toAdd = "";
			List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			Integer totalImages = imagesWithSpecies.size();
			// Activity / All
			toAdd = toAdd + "                   All months         Jan              Feb              Mar              Apr              May              Jun              Jul              Aug              Sep              Oct              Nov              Dec\n";
			toAdd = toAdd + "    Hour        Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency\n";

			int[] totals = new int[13];
			int[] totalActivities = new int[13];

			// 12 months + all months
			for (int i = -1; i < 12; i++)
			{
				Integer activity = 0;
				// -1 = all months
				if (i == -1)
					activity = analysis.activityForImageList(imagesWithSpecies);
				else
					activity = analysis.activityForImageList(new PredicateBuilder().monthOnly(i).query(imagesWithSpecies));
				totalActivities[i + 1] = activity;
			}

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
				toAdd = toAdd + String.format("%02d:00-%02d:00   ", i, i + 1);
				// 12 months
				for (int j = -1; j < 12; j++)
				{
					Integer activity = 0;
					// -1 = all months
					if (j == -1)
						activity = analysis.activityForImageList(imagesWithSpeciesAtTime);
					else
						activity = analysis.activityForImageList(new PredicateBuilder().monthOnly(j).query(imagesWithSpeciesAtTime));

					if (activity != 0)
						toAdd = toAdd + String.format("%6d %10.3f", activity, (double) activity / totalActivities[j + 1]);
					else
						toAdd = toAdd + "                 ";
					totals[j + 1] = totals[j + 1] + activity;
				}
				toAdd = toAdd + "\n";
			}

			toAdd = toAdd + "Total         ";

			for (int i = 0; i < totals.length; i++)
				toAdd = toAdd + String.format("%6d    100.000", totals[i]);

			toAdd = toAdd + "\n";

			// Print the header first
			toReturn = toReturn + String.format("%-28s (%6d/ %6d)\n", species.getName(), totals[0], totalImages);

			toReturn = toReturn + toAdd;

			toReturn = toReturn + "\n";
		}

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * A table showing the similarity comparison of activity patterns using hourly frequency is given. The number in the table shows the squart root
	 * of the sum of the squared differencs by hour for each species pair. Freqency is used because the number of records used to calcluate activity
	 * patterns generally differs for each species. If a pair of species has similar activity patterns then the value in the table will be low. If two
	 * species have very different activity patterns, one being diurnal, the other nocturnal for instance, the value in the table will be high.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesPairsActivitySimilarity()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES PAIRS ACTIVITY SIMILARITY (LOWER IS MORE SIMILAR)\n";

		toReturn = toReturn + "                            ";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-8s ", StringUtils.left(species.getName(), 8));
		}

		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-27s", species.getName());
			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
				List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(other).query(analysis.getImagesSortedByDate());
				int totalActivity = analysis.activityForImageList(imagesWithSpecies);
				int totalActivityOther = analysis.activityForImageList(imagesWithSpeciesOther);

				double activitySimilarity = 0;

				// 24 hrs
				for (int i = 0; i < 24; i++)
				{
					List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
					List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
					double activity = analysis.activityForImageList(imagesWithSpeciesAtTime);
					double activityOther = analysis.activityForImageList(imagesWithSpeciesAtTimeOther);
					double frequency = activity / totalActivity;
					double frequencyOther = activityOther / totalActivityOther;
					double difference = frequency - frequencyOther;
					// Frequency squared
					activitySimilarity = activitySimilarity + difference * difference;
				}

				toReturn = toReturn + String.format("%6.3f   ", activitySimilarity);
			}
			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The species pair that has the most similar activity pattern is compared. Only those species with 25 or more pictures are used.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciePairMostSimilar()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES PAIR MOST SIMILAR IN ACTIVITY (FREQUENCY)\n";
		toReturn = toReturn + "  Consider those species with 25 or more pictures\n";

		Species lowest = null;
		Species lowestOther = null;
		double lowestFrequency = Double.MAX_VALUE;

		for (Species species : analysis.getAllImageSpecies())
		{
			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
				List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(other).query(images);
				int totalImages = imagesWithSpecies.size();
				int totalImagesOther = imagesWithSpeciesOther.size();
				double activitySimilarity = 0;

				if (totalImages >= 25 && totalImagesOther >= 25 && !species.equals(other))
				{
					// 24 hrs
					for (int i = 0; i < 24; i++)
					{
						List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
						List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
						double numImages = imagesWithSpeciesAtTime.size();
						double numImagesOther = imagesWithSpeciesAtTimeOther.size();
						double frequency = numImages / totalImages;
						double frequencyOther = numImagesOther / totalImagesOther;
						double difference = frequency - frequencyOther;
						// Frequency squared
						activitySimilarity = activitySimilarity + difference * difference;
					}

					activitySimilarity = Math.sqrt(activitySimilarity);

					if (lowestFrequency >= activitySimilarity)
					{
						lowestFrequency = activitySimilarity;
						lowest = species;
						lowestOther = other;
					}
				}
			}
		}

		if (lowest != null)
		{
			toReturn = toReturn + String.format("Hour            %-28s %-28s\n", lowest.getName(), lowestOther.getName());

			List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(lowest).query(images);
			List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(lowestOther).query(images);
			int totalImages = imagesWithSpecies.size();
			int totalImagesOther = imagesWithSpeciesOther.size();
			double activitySimilarity = 0;

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
				List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
				double numImages = imagesWithSpeciesAtTime.size();
				double numImagesOther = imagesWithSpeciesAtTimeOther.size();
				double frequency = numImages / totalImages;
				double frequencyOther = numImagesOther / totalImagesOther;
				double difference = frequency - frequencyOther;
				// Frequency squared
				activitySimilarity = activitySimilarity + difference * difference;

				toReturn = toReturn + String.format("%02d:00-%02d:00     %5.3f                        %5.3f\n", i, i + 1, frequency, frequencyOther);
			}
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * Using the Ch-squared statistic activity patterns of paired species are analyzed and results presented in species x species table. The null
	 * hypothesis H0: Species A and B have similar activity patterns at 95% is tested. If the pattern is significantly similar then a "+" is entered
	 * for A x B, otherwise the pattern is not significcantly similar and is indicated by a"0" in the table. Only those species that have 25 or more
	 * records are considered.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printChiSquareAnalysisPairedActivity()
	{
		String toReturn = "";

		toReturn = toReturn + "CHI-SQUARE ANALYSIS OF PAIRED ACTIVITY PATTERNS\n";
		toReturn = toReturn + "  H0: Species A and B have similar activity patterns at 95%\n";
		toReturn = toReturn + "  Significant = X, Not significant = Blank\n";
		toReturn = toReturn + "  Consider only species with >= 25 pictures\n";

		toReturn = toReturn + "                            ";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-8s ", StringUtils.left(species.getName(), 8));
		}

		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			int totalImages = imagesWithSpecies.size();
			if (totalImages >= 25)
			{
				toReturn = toReturn + String.format("%-28s", species.getName());
				for (Species other : analysis.getAllImageSpecies())
				{
					List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(other).query(images);
					int totalImagesOther = imagesWithSpeciesOther.size();
					double activitySimilarity = 0;

					// 24 hrs
					for (int i = 0; i < 24; i++)
					{
						List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
						List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
						double numImages = imagesWithSpeciesAtTime.size();
						double numImagesOther = imagesWithSpeciesAtTimeOther.size();
						double frequency = numImages / totalImages;
						double frequencyOther = numImagesOther / totalImagesOther;
						double difference = frequency - frequencyOther;
						// Frequency squared
						activitySimilarity = activitySimilarity + difference * difference;
					}

					double chiSquare = (1 - activitySimilarity) / 1.0;

					if (chiSquare >= 0.95 && imagesWithSpeciesOther.size() >= 25)
						toReturn = toReturn + "   X     ";
					else
						toReturn = toReturn + "         ";
				}
				toReturn = toReturn + "\n";
			}
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * Using Northern hemisphere seasons of winter (Dec-Jan-Feb), spring (Mar-Apr-May), summer (Jun-Jul-Aug), and fall (Sep-Oct-Nov) activity patterns
	 * for each species are presented in a table. The table shows the number of records used in the actvity calculation and the frequency for each
	 * sason. To compare the seasonal activity patterns requires knowning the number of independent pictures recorded in each season normalied by the
	 * number of camera trap days (Pictures/Effort) for the season, and the proportion of the number of records divided by the total number of records
	 * for the all four seasons (Visitation proportion). That is, Visitation proportion is computed by summing Picture/Effort for all seasons, then
	 * dividing each season by the sum. This gives the proportion of records (based on indepdenent pictures, not the number of pictures used to create
	 * activity). Note that more records likely result from greater effort, hence the number of records must be normalizedby effort. The total number
	 * of records for each season is given.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printActivityPatternsSeason()
	{
		String toReturn = "";

		toReturn = toReturn + "ACTIVITY PATTERNS BY SEASON\n";
		toReturn = toReturn + "  Activity in one-hour segments by season\n";

		int[][] seasons = new int[][]
		{
				{ 11, 0, 1 }, // 1
				{ 2, 3, 4 }, // 2
				{ 5, 6, 7 }, // 3
				{ 8, 9, 10 } }; // 4

		int[] lengthPerSeason = new int[4];
		int[] monthlyTotals = new int[12];
		for (Location location : analysis.getAllImageLocations())
		{
			List<ImageEntry> withLocation = new PredicateBuilder().locationOnly(location).query(images);
			Calendar firstCal = DateUtils.toCalendar(analysis.getFirstImageInList(withLocation).getDateTaken());
			Calendar lastCal = DateUtils.toCalendar(analysis.getLastImageInList(withLocation).getDateTaken());
			Integer firstMonth = firstCal.get(Calendar.MONTH);
			Integer lastMonth = lastCal.get(Calendar.MONTH);
			Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
			Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
			Calendar calendar = Calendar.getInstance();
			toReturn = toReturn + String.format("%-28s", location.getName());
			int monthTotal = 0;
			for (int i = 0; i < 12; i++)
			{
				int monthValue = 0;
				if (firstMonth == lastMonth && firstMonth == i)
					monthValue = lastDay - firstDay + 1;
				else if (firstMonth == i)
					monthValue = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH) - firstDay + 1;
				else if (lastMonth == i)
					monthValue = lastDay;
				else if (firstMonth < i && lastMonth > i)
				{
					calendar.set(Calendar.MONTH, i);
					monthValue = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				}

				toReturn = toReturn + String.format(" %2d    ", monthValue);
				monthTotal = monthTotal + monthValue;
				monthlyTotals[i] = monthlyTotals[i] + monthValue;
			}
			toReturn = toReturn + monthTotal + "\n";
		}

		for (int[] season : seasons)
			for (int month : season)
				lengthPerSeason[ArrayUtils.indexOf(seasons, season)] = lengthPerSeason[ArrayUtils.indexOf(seasons, season)] + monthlyTotals[month];

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());

			toReturn = toReturn + species.getName() + "\n";
			toReturn = toReturn + "                     Dec-Jan-Feb           Mar-Apr-May           Jun-Jul-Aug           Sep-Oct-Nov\n";
			toReturn = toReturn + String.format("Camera trap days    ");

			for (Integer length : lengthPerSeason)
				toReturn = toReturn + String.format("%7d               ", length);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Number of pictures  ";
			int[] imagesPerSeason = new int[4];
			for (int i = 0; i < 4; i++)
			{
				List<ImageEntry> seasonWithSpecies = new PredicateBuilder().monthOnly(seasons[i]).query(withSpecies);
				Integer activity = analysis.activityForImageList(seasonWithSpecies);
				toReturn = toReturn + String.format("%7d               ", activity);
				imagesPerSeason[i] = activity;
			}
			toReturn = toReturn + "\n";
			toReturn = toReturn + "Pictures/Effort        ";
			double total = 0;
			double ratios[] = new double[4];
			for (int i = 0; i < 4; i++)
			{
				double currentRatio = 0;
				if (lengthPerSeason[i] != 0)
					currentRatio = (double) imagesPerSeason[i] / lengthPerSeason[i];
				toReturn = toReturn + String.format("%5.4f                ", currentRatio);
				ratios[i] = currentRatio;
				total = total + currentRatio;
			}
			toReturn = toReturn + "\n";
			toReturn = toReturn + "Visitation proportion  ";
			for (int i = 0; i < 4; i++)
			{
				if (total != 0)
					toReturn = toReturn + String.format("%5.4f                ", ratios[i] / total);
				else
					toReturn = toReturn + String.format("%5.4f                ", 0f);
			}

			toReturn = toReturn + "\n";

			String toAdd = "";

			toAdd = toAdd + "           Hour        Number      Freq      Number      Freq      Number      Freq      Number      Freq\n";

			int[] hourlyTotals = new int[4];

			// 24 hrs
			for (int j = 0; j < 24; j++)
			{
				List<ImageEntry> withSpeciesAtTime = new PredicateBuilder().timeFrame(j, j + 1).query(withSpecies);

				toAdd = toAdd + String.format("       %02d:00-%02d:00    ", j, j + 1);

				// 4 seasons
				for (int i = 0; i < 4; i++)
				{
					List<ImageEntry> withSpeciesAtTimeInSeason = new PredicateBuilder().monthOnly(seasons[i]).query(withSpeciesAtTime);
					List<ImageEntry> withSpeciesInSeason = new PredicateBuilder().monthOnly(seasons[i]).query(withSpecies);
					Integer numPics = analysis.activityForImageList(withSpeciesAtTimeInSeason);
					Integer totalPics = analysis.activityForImageList(withSpeciesInSeason);
					double frequency = 0;
					if (totalPics != 0)
						frequency = (double) numPics / totalPics;
					else
						frequency = 0;

					hourlyTotals[i] = hourlyTotals[i] + numPics;

					toAdd = toAdd + String.format("%5d        %5.3f    ", numPics, frequency);
				}

				toAdd = toAdd + "\n";
			}

			toAdd = toAdd + "       Hourly pics  ";
			for (int i = 0; i < hourlyTotals.length; i++)
				toAdd = toAdd + String.format("%7d               ", hourlyTotals[i]);

			toAdd = toAdd + "\n";

			toReturn = toReturn + toAdd + "\n";
		}

		return toReturn;
	}
}
