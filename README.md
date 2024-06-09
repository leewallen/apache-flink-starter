Apache Flink Starter
===================
A starting point for an [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-master/) project that powers a data pipeline involving Kafka and the ELK stack. There are also various administrative tools like Kafdrop. All these systems are able to run under docker.

## Pre-Requisites

1. Docker on [Mac](https://download.docker.com/mac/stable/Docker.dmg)

1. [Gradle](https://gradle.org) - You have a few options here
    + If you're using Intellij, just make sure it's enabled.
    + Run `brew install gradle`

## Up & Running

Let's first clone the repo and fire up our system,

```
git clone git@github.com:leewallen/apache-flink-starter.git ~/projects/apache-flink-starter
cd ~/projects/apache-flink-starter;./gradlew kafkaUp
```
Now you have a single node Kafka cluster with various admin tools to make life a little easier. See the [Kafka cluster repo](https://github.com/aedenj/kafka-cluster-starter) for its operating details.

## Running the App

The sample job in this repo reads from a topic named `source` and writes to a topic named `destination`.
There are a couple of ways of running this job depending on what you're trying to accomplish.

First, let's setup the kafka topics. Run `./gradlew createTopics`.

### Locally

For quick feedback it's easiest to run the job locally,

1. If you're using Intellij, use the usual methods.
1. On the command line run `./gradlew shadowJar run`

### Using the Job Cluster

Run `./gradlew shadowJar startJob`. This will run the job within a job cluster that is setup in `flink-job-cluster.yml`. That cluster will run against the Kafka cluster started earlier.


### Observing the Job in Action

After starting the job with one of the methods above, let's observe it reading an writing a message from one Kafak topic to another.

1. Start the job using one of the methods above.
1. In a new terminal start a Kafka producer by running `./scripts/start-kafka-producer.sh`
1. You'll see the prompt `>`. Enter the message `1:{ message: "Hello World!" }`
1. Navigate to the [Kafdrop](http://localhost:8001/#/) and view the messages both the `source` and `destination` topics. Be sure to change format to default or else you will not see any messages.

You should see the message `1:{ message: "Hello World!" }` in both topics.

### Metrics

Metrics are exposed via Prometheus Metric Reporter and Grafana.
