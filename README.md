# epidemes mic<em>R<sub>0</sub></em>simulator
Synthetic populations (demes) for epidemiological research

## Quick Start

For a simple scenario run, have [Java SE Platform 8+](http://www.oracle.com/technetwork/java/javase/downloads/) installed, along with [Maven 3+](https://maven.apache.org/download.cgi) and [Git](https://git-scm.com/download), or your favorite IDE which includes them, e.g. [Eclipse EE](https://www.eclipse.org/downloads/eclipse-packages/) or [NetBeans](https://netbeans.org/downloads/),= (possibly extended with tooling for YAML, Docker, Gradle, etc.) then follow these steps:

```
git clone https://github.com/krevelen/epidemes.git
cd epidemes/java
mvn install -Dmaven.test.skip=true
java -jar epidemes-demo/target/epidemes-demo.jar
```

DOCKERIZE!

# Features

For the purpose of creating a census data driven synthetic Dutch population fr epidemiological research and policy support, this project applies recent advances with respect to simulating household demographics ([Geard et al., 2013](http://dx.doi.org/10.18564/jasss.2098)) and spatial contact patterns in large-scale environments ([Zhang et al., 2016](http://dx.doi.org/10.18564/jasss.3148)). Furthermore the *Four C* model of individual vaccination hesitancy behavior ([Betsch et al., 2015](http://dx.doi.org/10.1177/2372732215600716)) has been formalized and implemented to study the effects of national vaccination programs under various conditions.

Related work includes for instance, POHEM ([Hennessy et al., 2015](http://dx.doi.org/10.1186/s12963-015-0057-x)), [FRED](https://github.com/PublicHealthDynamicsLab/FRED/wiki) ([Grefenstette et al., 2013](http://dx.doi.org/10.1186/1471-2458-13-940)), EpiSimS/OPPIE ([Del Valle et al., 2012](http://dx.doi.org/10.1007/978-1-4614-5474-8_4)), and EpiFast ([Bisset et al., 2009](http://dx.doi.org/10.1145/1542275.1542336)).

## Household Demography

## Spatial Contacts

## Vaccination Hesitancy
The following model is derived loosely from definitions given by [Betsch et al. (2015)](http://dx.doi.org/10.1177/2372732215600716) and validated with data vaccination attitudes and behaviors obtained from several studies, including a large-scale serosurveillance study held among the Dutch population ([ISRCTN 20164309](http://www.trialregister.nl/trialreg/admin/rctview.asp?TC=977), [Van der Klis et al., 2009](https://www.ncbi.nlm.nih.gov/pubmed/19687529)).
 
Data from these studies show that for the Dutch situation, convenience of vaccination events is considered to have little effect on vaccination behaviors, since the events are conveniently combined with regular visits to municipal health services aimed at tracking and supporting individual welfare of newborns and infants. Furthermore, the data show that mental barriers against vaccination are strongly increased by certain protestant religious convictions, whereas alternative medical and health convictions (e.g. homeopathy as inspired by Hahneman, and anthroposophy as inspired by Steiner) appear to have a more nuanced correlation with individual vaccination hesitancy.

The **Four C model** formalization applied here is as follows. Given an individual *i* having several opinions and/or experiences *j* from the social network (including self), with weight *w* representing the individual's appreciation of the opinion's source exceeding the individual's minimum **calculation** threshold for inclusion, the mental barrier resulting from a weighted product of each source's **confidence** in the vaccine system and/or the source's **complacency** in disease risk absence will lead to **hesitancy** (i.e. vaccination delay) iff it is not sufficiently relieved by the **convenience** of the vaccination event *e* at hand:

`hesitancy(i,e): product_j(w_j ^ confidence_i,j[w_j > calculation_i] * w_j ^ -complacency_i,j[w_j > calculation_i]) > convenience_i,e`
