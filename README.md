# metarKt

Up to Kotlin 2.1.20 use
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alexmaryin.metarkt/parser?style=plastic)](https://repo1.maven.org/maven2/io/github/alexmaryin/metarkt/)

Since Kotlin 2.2.0 use 
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alexmaryin.metarkt/metarkt?style=plastic)](https://repo1.maven.org/maven2/io/github/alexmaryin/metarkt/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=plastic)](https://opensource.org/licenses/MIT)

_Kotlin multiplatform library for parsing METAR raw data into pure Kotlin data classes._

METARs typically come from airports or permanent weather observation stations. 
Reports are generated once an hour or half-hour at most stations, but if conditions change significantly at a staffed location, 
a report known as a special (SPECI) may be issued. There are stations that make regular reports more often.(Such as KPLU which reports 3 per hour.) 
Some METARs are encoded by automated airport weather stations located at airports, military bases, and other sites. 
Some locations still use augmented observations, which are recorded by digital sensors, encoded via software, and then reviewed by certified 
weather observers or forecasters prior to being transmitted. Observations may also be taken by trained observers or forecasters who manually 
observe and encode their observations prior to transmission.

A typical METAR contains data for the temperature, dew point, wind direction and speed, precipitation, cloud cover and heights, visibility, 
and barometric pressure. A METAR may also contain information on precipitation amounts, lightning, and other information that would be of
interest to pilots or meteorologists such as a pilot report or PIREP, colour states and runway visual range (RVR). [Read more.](https://en.wikipedia.org/wiki/METAR)


This is an example for Ulyanovsk Central Airport METAR:

`UWLL 251200Z 16008MPS 8000 -SN BLSN OVC013 M09/M13 Q1008 R20/820242 NOSIG RMK QFE746/0994`

## Install dependency for Kotlin up to 2.1.20

### Maven
```xml
<dependency>
  <groupId>io.github.alexmaryin.metarkt</groupId>
  <artifactId>parser</artifactId>
  <version>1.0.1</version>
</dependency>
```

### Gradle
```groovy
implementation 'io.github.alexmaryin.metarkt:parser:1.0.1'
```

### Kotlin-DSL
```kotlin
implementation("io.github.alexmaryin.metarkt:parser:1.0.1")
```

## Install dependency for since Kotlin 2.2.0+

### Maven
```xml
<dependency>
  <groupId>io.github.alexmaryin.metarkt</groupId>
  <artifactId>metarkt</artifactId>
  <version>1.0.3</version>
</dependency>
```

### Gradle
```groovy
implementation 'io.github.alexmaryin.metarkt:parser:1.0.3'
```

### Kotlin-DSL
```kotlin
implementation("io.github.alexmaryin.parser:metarkt:1.0.3")
```

## Using parser

The main interface implemented in library is `MetarParser`. It's including companion object with
constructor of standard implementation: `MetarParser.current()`.

Returning data type of `Metar` is composed of following values:
```
    val station: String?,
    val reportTime: LocalDateTime?,
    val wind: Wind?,
    val visibility: Visibility?,
    val phenomenons: Phenomenons?,
    val clouds: List<CloudLayer>,
    val temperature: Temperature?,
    val pressureQNH: PressureQNH?,
    val ceilingAndVisibilityOK: Boolean,
    val raw: String
```
Primitive properties:
- `station` is parsed ICAO code of Airport or Weather observation station, i.e. UWLL
- `reportTime` report time in LocalDataTime format from [kotlinx-datatime](https://github.com/Kotlin/kotlinx-datetime) library.
- `ceilingAndVisibilityOK` return `true` for [CAVOK](https://en.wiktionary.org/wiki/CAVOK) status
- `raw` original METAR string

### Wind object

- `direction` value in degrees from which wind is blowing
- `variable` is `true` if the direction is undefined
- `speed` average speed of the wind in original units
- `speedUnits` units of the speed from enum `WindUnit` (KT, MPS, KPH)
- `gusts` max speed of the gusts
- `isCalm` return `true` if the wind is calm
- `speedKt`, `gustsKt` - speed in Knots
- `speedMps`, `gustsMps` - speed in Meters per second

### Visibility object

- `distAll` either visibility in defined units for the airfield or `null` if the visibility
defined by individual direction or runway
- `distUnits` units of visibility distance from enum `VisibilityUnit` (METERS, SM)
- `byDirections` list of `VisibilityByDir` objects which contains:
  - `dist` visibility in defined units
  - `direction` direction of this visibility distance from enum `VisibilityDirection` (NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST)
- `byRunways` list of `VisibilityByRunway` objects which contains:
  - `dist` visibility in defined units
  - `runway` name of the runway


### Phenomenons object

- `phenomemons` is the list of `WeatherPhenomenon` objects which contains:
  - `group` set of `Phenomenons` enum items (DRIZZLE, RAIN, SNOW, SNOW_GRAINS, ICE_PELLETS, SMALL_HAIL,
    HAIL, SHOWER, FREEZE, THUNDERSTORM, DUST_STORM, SANDSTORM, FOG, IN_VICINITY, SHALLOW, PARTIAL, PATCHES, 
    MIST, HAZE, SMOKE, DUST, BLOWING, SQUALL, ICE_CRYSTALS, VOLCANIC_ASH, DRIFTING, SAND)
  - `intensity` flag of the intensity from enum `PhenomenonIntensity` (NONE, HIGH, LIGHT) for this group

### Clouds

- `clouds` is the list of `CloudLayer` objects which contains:
  - `type` of the cloud layer from enum `CloudsType` (CLEAR, NIL_SIGNIFICANT, FEW, SCATTERED, BROKEN, OVERCAST)
  - `lowMarginFt` low margin of the cloud layer in Feet
  - `cumulusType` `null` if the clouds are not generating precipitation or one of the enum `CumulusType` type (CUMULONIMBUS, TOWERING_CUMULUS)

### Temperature object

- `air` temperature of the air in Celsius
- `dewPoint` temperature of the dew point

### PressureQNH object

- `hPa` sea level pressure in Hectopascals
- `inHg` sea level pressure in the Inch of mercury

### License

Copyright 2021-2025 ALex Maryin

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

