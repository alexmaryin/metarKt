# metarKt

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

## Install dependency

### Maven
```
<dependency>
  <groupId>io.github.alexmaryin.metarkt</groupId>
  <artifactId>parser</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle
```
implementation 'io.github.alexmaryin.metarkt:parser:1.0.0'
```

### Kotlin-DSL
```
implementation("io.github.alexmaryin.metarkt:parser:1.0.0")
```

