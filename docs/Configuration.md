# Configuring OpenTripPlanner

_Note: if you are familiar with OTP1 configuration and are migrating to OTP2, please read the
[OTP2 Migration Guide](OTP2-MigrationGuide.md) to learn what has changed._

## Base Directory

On the OTP2 command line you must always specify a single directory after all the switches. This
tells OTP2 where to look for any configuration files. By default OTP will also scan this directory
for input files to build a graph (GTFS, OSM, elevation, and base street graphs) or the `graph.obj`
file to load when starting a server.

A typical OTP2 directory for a New York City graph might include the following:

```
otp-config.json
build-config.json
router-config.json
new-york-city-no-buildings.osm.pbf
nyc-elevation.tiff
long-island-rail-road.gtfs.zip
mta-new-york-city-transit.gtfs.zip
port-authority-of-new-york-new-jersey.gtfs.zip
graph.obj
```

You could have more than one of these directories if you are building separate graphs for separate
regions. Each one should contain one or more GTFS feeds, a PBF OpenStreetMap file, some JSON
configuration files, and any output files such as `graph.obj`. For convenience, especially if you
work with only one graph at a time, you may want to place your OTP2 JAR file in this same directory.
Note that file types are detected through a case-insensitive combination of file extension and words
within the file name. GTFS file names must end in `.zip` and contain the letters `gtfs`, and OSM
files must end in `.pbf`.

It is also possible to provide a list of input files in the configuration, which will override the
default behavior of scanning the base directory for input files. Scanning is overridden
independently for each file type, and can point to remote cloud storage with arbitrary URIs.
See [the storage section](Configuration.md#Storage) for further details.

## Three Scopes of Configuration

OTP is configured via three configuration JSON files which are read from the directory specified on
its command line. We try to provide sensible defaults for every option, so all three of these files
are optional, as are all the options within each file. Each configuration file corresponds to
options that are relevant at a particular phase of OTP usage.

Options and parameters that are taken into account during the graph building process will be "baked
into" the graph, and cannot be changed later in a running server. These are specified
in `build-config.json`. Other details of OTP operation can be modified without rebuilding the graph.
These run-time configuration options are found in `router-config.json`. Finally, `otp-config.json`
contains simple switches that enable or disable system-wide features.

## Configuration types

The OTP configuration files use the JSON file format. OTP allows comments and unquoted field names
in the JSON configuration files to be more human-friendly. OTP supports all the basic JSON types:
nested objects `{...}`, arrays `[]`, numbers `789.0` and boolean `true` or `false`. In addition to
these basic types some configuration parameters are parsed with some restrictions. In the
documentation below we will refer to the following types:

| Type            | Description                                                                                                                                                                                                                                                         | Examples                                                                                                                                                                                                       |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| boolean         | This is the Boolean JSON type.                                                                                                                                                                                                                                      | `true` or `false`                                                                                                                                                                                              |
| number          | This is the Number JSON type.                                                                                                                                                                                                                                       | `1`, `5`, `3.14`                                                                                                                                                                                               |
| string          | A quoted string. This is the String JSON type.                                                                                                                                                                                                                      | `"This is a string!"`                                                                                                                                                                                          |
| _Type_[]        | Array of of given Type. This is the Array JSON type.                                                                                                                                                                                                                | `[ 1, 2, 3 ]`                                                                                                                                                                                                  |
| double          | A decimal floating point _number_. 64 bit.                                                                                                                                                                                                                          | `3.14`                                                                                                                                                                                                         |
| integer         | A decimal integer _number_. 32 bit.                                                                                                                                                                                                                                 | `1`, `-7`, `2100200300`                                                                                                                                                                                        |
| long            | A decimal integer _number_. 64 bit.                                                                                                                                                                                                                                 | `-1234567890123456789`                                                                                                                                                                                         |
| enum            | A fixed set of string literals.                                                                                                                                                                                                                                     | BicycleOptimize: `"QUICK"`, `"SAFE"` ...                                                                                                                                                                       |
| enum-map        | List of key/value pairs, where the key is a enum and the value can be any given type.                                                                                                                                                                               | `{ RAIL: 1.2, BUS: 2.3 }`                                                                                                                                                                                      |
| enum-set        | List of enum string values                                                                                                                                                                                                                                          | `[ "RAIL", "TRAM" ]`                                                                                                                                                                                           |
| locale          | _`Language[\_country[\_variant]]`_. A Locale object represents a specific geographical, political, or cultural region. For more information see the [Java 11 Locale](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Locale.html).           | `en_US`, `nn_NO`                                                                                                                                                                                               |
| date            | Local date. The format is _YYYY-MM-DD_ (ISO-8601).                                                                                                                                                                                                                  | `2020-09-21`                                                                                                                                                                                                   |
| date or period  | A _local date_, or a _period_ relative to today. The local date has the format `YYYY-MM-DD` and the period has the format `PnYnMnD` or `-PnYnMnD` where `n` is a integer number.                                                                                    | `P1Y` is one year from now, `-P3M2D` means 3 months and 2 days ago, and `P1D` means tomorrow.                                                                                                                  |
| duration        | A _duration_ is a amount of time. The format is `PnDTnHnMnS` or `nDnHnMnS` where `n` is a  integer number. The `D`(days), `H`(hours), `M`(minutes) and `S`(seconds) are not case sensitive.                                                                         | `3h` is 3 hours, `2m` means 2 minutes, and `1d5h2m3s` is 1 day, 5 hours, 2 minutes and 3 seconds. Use the "PT" form with negative values like `-P2dT-1s` and `P-2dT1s` (both is minus 2 days plus one second). |
| regexp pattern  | A regular expression pattern used to match a sting.                                                                                                                                                                                                                 | `"$^"` matches an empty string. `"gtfs"` matches `"A-*gtfs*-file.zip"`. `"$\w{3})-.*\.xml^"` matches a filename with 3 alpha-numeric characters in the beginning of the filename and _.xml_ as file extension. |
| uri             | An URI path to a resource like a file or a URL. Relative URIs are resolved relative to the OTP base path.                                                                                                                                                           | `"gs://bucket/path/a.obj"` `"http://foo.bar/"` `"file:///Users/x/local/file"` `"myGraph.obj"` `"../street/streetGraph-${otp.serialization.version.id}.obj"`                                                    |
| linear function | A linear function with one input parameter(x) used to calculate a value. Usually used to calculate a limit. For example to calculate a limit in seconds to be 1 hour plus 2 times the value(x) use: `3600 + 2.0 x`, to set an absolute value(3000) use: `3000 + 0x` | `"600 + 2.0 x"`                                                                                                                                                                                                |

## System environment and project information substitution

OTP support injecting system environment variables and project information parameters into the
configuration. A pattern like `${VAR_NAME}` in a configuration file is substituted with an
environment variable with name `VAR_NAME`. The substitution is done BEFORE the JSON is parsed, so
both json keys and values is subject to substitution. This is useful if you want OTPs version number
to be part of the _graph-file-name_, or you want to inject credentials in a cloud based deployment.

```JSON
{
    "gsCredentials": "${GCS_SERVICE_CREDENTIALS}",
    "graph": "file:///var/otp/graph-${otp.serialization.version.id}.obj"
}
```     

In the example above the environment variable `GCS_SERVICE_CREDENTIALS` on the local machine where
OTP is deployed is injected into the config. Also, the OTP serialization version id is injected.

The project information variables available are:

- `maven.version`
- `maven.version.short`
- `maven.version.major`
- `maven.version.minor`
- `maven.version.patch`
- `maven.version.qualifier`
- `git.branch`
- `git.commit`
- `git.commit.timestamp`
- `graph.file.header`
- `otp.serialization.version.id`

## Config version

All three configuration files have an optional `configVersion` property. The property can be used to
version the configuration in a deployment pipeline. The `configVersion` is not used by OTP in any
way, but is logged at startup and is available as part of the _server-info_ data in the REST API.
The intended usage is to be able to check which version of the configuration the graph was build
with and which version the router uses. In an deployment with many OTP instances it can be useful to
ask an instance about the version, instead of tracking the deployment pipline backwards to find the
version used. How you inject a version into the configuration file is up to you, but you can do it
in your build-pipline, at deployment time or use system environment variable substituton.

## OTP Serialization version id and _Graph.obj_ file header

OTP has a _OTP Serialization Version Id_ maintained in the pom.xml_ file. OTP store the id in the
serialized _Graph.obj_ file header, allowing OTP the check for compatibility issues when loading the
graph. The header info is available to configuration substitution:

- `${graph.file.header}` Will expand to: `OpenTripPlannerGraph;0000007;`
- `${otp.serialization.version.id}` Will expand to: `7`

The intended usage is to be able to have a graph build pipeline which "knows" which graph that
matches OTP planner instances. For example, you may build new graphs for every OTP serialization
version id in use by the planning OPT instances you have deploied and plan to deploy. This way you
can roll forward and backward new OTP instances without worring about building new graphs.

There are various ways to access this information. To get the `Graph.obj` serialization version id
you can run the following bash command:

- `head -c 29 Graph.obj ==>  OpenTripPlannerGraph;0000007;` (file header)
- `head -c 28 Graph.obj | tail -c 7 ==>  0000007`  (version id)

The Maven _pom.xml_, the _META-INF/MANIFEST.MF_, the OTP command line(`--serVerId`), log start-up
messages and all OTP APIs can be used to get the OTP Serialization Version Id.

## Include file directive

It is possible to inject the contents of another file into a configuration file. This makes it
possible to keep parts of the configuration in separate files. To include the contents of a file,
use
`${includeFile:FILE_NAME}`. The `FILE_NAME` must be the name of a file in the configuration
directory. Relative paths are not supported.
<p>
To allow both files (the configuration file and the injected file) to be valid JSON files, a special
case is supported. If the include file directive is quoted, then the quotes are removed, if the 
text inserted is valid JSON (starts with `{` and ends with `}`). 

Variable substitution is performed on configuration file after the include file directive; Hence
variable substitution is also performed on the text in the injected file.

Here is an example including variable substitution, assuming version 2.1.0 of OTP:

```JSON
// build-config.json
{
  "transitFeeds" : "${includeFile:transit.json}"
} 
``` 

```JSON
// transit.json
[
  {
  "source": "netex-v${FEED_VERSION}.obj"
}
]
``` 

The result will look like this:

```JSON
{
      "transitFeeds": [
        {
          "source": "netex-v2_0.obj"
        }
      ]
} 
``` 

# System-wide Configuration

Using the file `otp-config.json` you can enable or disable different APIs and experimental
[Sandbox Extensions](SandboxExtension.md). By default, all supported APIs are enabled and all
sandbox features are disabled. So for most OTP2 use cases it is not necessary to create this file.
Features that can be toggled in this file are generally only affect the routing phase of OTP2 usage,
but for consistency all such "feature flags", even those that would affect graph building, are
managed in this one file. See
the [OTPFeature](https://github.com/opentripplanner/OpenTripPlanner/blob/dev-2.x/src/main/java/org/opentripplanner/util/OTPFeature.java)
Java class for an enumeration of all available features and their default settings. Here is an
example:

```JSON
// otp-config.json
{
    "otpFeatures" : {
        "APIBikeRental" : false,
        "ActuatorAPI" : true
    }
}
```

## OTP Features

Here is a list of all features which can be toggled on/off.

| Feature                                | Description                                                                                                                                                                                     | Enabled by default | Sandbox |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------|---------|
| `APIBikeRental`                        | Enable the bike rental endpoint                                                                                                                                                                 | yes                | no      |
| `APIServerInfo`                        | Enable the server info endpoint                                                                                                                                                                 | yes                | no      |
| `APIGraphInspectorTile`                | Enable the inspector  endpoint for graph information for inspection/debugging purpose                                                                                                           | yes                | no      |
| `APIUpdaterStatus`                     | Enable endpoint for graph updaters status                                                                                                                                                       | yes                | no      |
| `ConsiderPatternsForDirectTransfers`   | Enable limiting transfers so that there is only a single transfer to each pattern.                                                                                                              | yes                | no      | 
| `OptimizeTransfers`                    | OTP will inspect all itineraries found and optimize where (which stops) the transfer will happen. Waiting time, priority and guaranteed transfers are taken into account.                       | yes                | no      |
| `MinimumTransferTimeIsDefinitive`      | If the minimum transfer time is a lower bound (default) or the definitive time for the transfer. Set this to true if you want to set a transfer time lower than what OTP derives from OSM data. | no                 | no      |
| `ParallelRouting`                      | Enable performing parts of the trip planning in parallel                                                                                                                                        | no                 | no      |
| `TransferConstraints`                  | Enforce transfers to happen according to the _transfers.txt_(GTFS) and Interchanges(NeTEx). Turing this _off_ will increase the routing performance a little.                                   | yes                | no      |
| `ActuatorAPI`                          | Enpoint for actuators (service health status)                                                                                                                                                   | no                 | yes     |
| `GoogleCloudStorage`                   | Enable Google Cloud Storage integration                                                                                                                                                         | no                 | yes     |
| `SandboxAPITransmodelApi`              | Enable Entur Transmodel(NeTEx) GraphQL API                                                                                                                                                      | no                 | yes     |
| `SandboxAPILegacyGraphQLApi`           | Enable (GTFS) GraphQL API                                                                                                                                                                       | no                 | yes     |
| `SandboxAPIMapboxVectorTilesApi`       | Enable Mapbox vector tiles API                                                                                                                                                                  | no                 | yes     |
| `SandboxAPIParkAndRideApi`             | Enable park-and-ride endpoint                                                                                                                                                                   | no                 | yes     |
| `TransferAnalyzer`                     | Analyze transfers during graph build                                                                                                                                                            | no                 | yes     |
| `FlexRouting`                          | Enable FLEX routing                                                                                                                                                                             | no                 | yes     |
| `FloatingBike`                         | Enable floating bike routing                                                                                                                                                                    | yes                | yes     |

# JVM configuration
This section contains general recommendations for tuning the JVM in a production environment.  
It focuses mainly on garbage collection configuration and memory settings.  
See [Garbage Collector Tuning](https://docs.oracle.com/en/java/javase/17/gctuning/introduction-garbage-collection-tuning.html) for general information on garbage collection.  
See [Large Pages in Java](https://kstefanj.github.io/2021/05/19/large-pages-and-java.html) and  [Transparent Huge Pages](https://shipilev.net/jvm/anatomy-quarks/2-transparent-huge-pages) for general information on large memory pages.

## OTP server
The OTP server processes concurrent routing requests in real time.  
The main optimization goal for the OTP server is minimizing response time.

### Garbage collector
- The G1 garbage collector (default since Java 9) offers a good compromise between low latency (i.e. low GC pause time) and GC overhead.
- If latency spikes are an issue, the ZGC garbage collector is an alternative. It produces in general more overhead than G1.  

### Memory settings 
- Using Large Memory Pages can reduce pressure on the TLB cache and increase performance.  
- It is in general not recommended to use large memory page in _Transparent Huge Page_ mode (`-XX:+UseTransparentHugePages`) for latency-sensitive applications, since memory is allocated on-demand and this can induce latency spikes if the memory is fragmented.  
  Thus _TLBFS_ mode (`-XX:+UseHugeTLBFS`) should be the first choice.
- If _TLBFS_ mode is not an option,  _Transparent Huge Page_ mode (`-XX:+UseTransparentHugePages`) can be used instead, with additional provisions to mitigate the risk of latency spikes:  
The physical memory can be committed upfront, at JVM startup time. This can be done by forcing a fixed heap size and pre-touching the memory.  
Example: `-Xms18g -Xmx18g -XX:-UseTransparentHugePages -XX:+AlwaysPreTouch`  

## Graph Builder
The Graph Builder is the non-interactive mode used to build street graphs and transit graphs.     
The main optimization goal for the Graph Builder is minimizing total build time.  

### Garbage collector
- In theory, the Parallel garbage collector offers the best throughput.  
In practice, it can be challenging to optimize the Parallel GC to build both a street graph and a transit graph, the memory usage patterns being different. 
- The G1 garbage collector provides in general a good compromise.  

### Memory settings 
- Using Large Memory Pages can reduce pressure on the TLB cache and increase performance.  
- Since latency is not an issue, Large Memory Pages can be used indifferently in _TLBFS_ mode (`-XX:+UseHugeTLBFS`) or _Transparent Huge Page_ mode (`-XX:+UseTransparentHugePages`)
