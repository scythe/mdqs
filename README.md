# US metropolitan areas by median population density

![Graph of cities by median population density with octiles](https://raw.githubusercontent.com/scythe/mdqs/master/city_pdqs.jpg)

The median population density for a metropolitan area, considered by ZIP code, is defined as the density of a zip code
such that half of the people in the metro area live in ZIP codes with lower population densities and half live in zip codes
with higher population densities. The octiles are defined similarly, with 12.5%, 25%, etc living in zip codes with densities
lower than the octile density. Where necessary, linear interpolation is applied, although this has a small effect.

## Why?

I wanted to find a way to compare the way people in different metropolitan areas actually live, and I was concerned that
densities of cities tend to be artifacts of the way municipal boundaries are drawn, while densities of counties (hence metros)
tend to be strongly affected by outliers (half of the "Miami metropolitan area" is an Everglades preserve). 

This leads to some surprising results. Most people would be shocked to find that the median Denverite lives in a denser
neighborhood than the median Bostonian. After all, Boston is a darling of the urbanist movement, and Denver is a recent growth
magnet in the "suburban" area. What seems to be happening is that lot sizes, rather than the presence of apartment buildings,
are the primary determinant of effective density for the majority of Americans in large metropolitan areas. Even though San Jose
is mostly single-family homes, they are mostly on small properties that lead to a higher density-in-reality for most residents
than what we observe in Philadelphia, where the majority of residents live in low-density sprawl, despite its larger urban core.

## Where did you get these data?

The population and area of ZIP codes were compiled by [Splitwise](http://splitwise.com) and made freely available based on
2010 Census data:

https://blog.splitwise.com/2014/01/06/free-us-population-density-and-unemployment-rate-by-zip-code/

Tables giving the relationship between ZIP codes and county boundaries (hence metropolitan areas) are available from HUD:

https://www.huduser.gov/portal/datasets/usps_crosswalk.html

These were compiled by a simple [Clojure script](/scythe/mdqs/quantiles.clj) into point estimates of the [quantile function](http://en.wikipedia.org/wiki/Quantile_function) which relates population fractions to densities. 

## Are the results reliable? Why not use census blocks?

I used ZIP codes simply because the data was easy to find. Census data is hidden within a Byzantine labyrinth of APIs with
little public documentation.

However, I do think that the results will prove robust. Most metropolitan areas listed contain hundreds of ZIP codes, which
gives plenty of granularity. The geometry of ZIP codes is generally determined by population and transportation networks and
there is no political reason their layout should be especially biased in certain metropolitan areas. Outliers such as 
'private' ZIP codes (which may be purchased from USPS) are not expected to have a large effect on the data since they tend
to contain a small fraction of the total population.

I would be interested in updating the graph to use census block data and/or incorporate the 2020 Census, although for now
I am taking a break from fighting the census data website, and decided to publish the graph as-is.


