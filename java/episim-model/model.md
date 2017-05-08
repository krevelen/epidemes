# Model

## Ecosystem

### Transactions

#### Transmission (Social + Spatial dynamics)

| Initiating role | Transaction | Executing role | Concerns        |
| ---------------:|:----------- |:-------------- |:--------------- |
          P1.Init -> | Redirection/rq    | @ P1.Director    | goals (abilities, beliefs)
   Redirection/ex -> | Plan/rq           | @ P1.Planner     | routines (person/location types, timing patterns)
          Plan/ex -> | Activity/rq       | @ P1.Activator   | meetings (contextual realization)
      Activity/ex -> | Contact/rq        | @ P2.Mixer       | relations (peer group, channel/medium, )
                     |               cc: | @ C2.Transmitter | (e.g. physical/sexual contact)
      Activity/ex -> | Transportation/rq | @ T.Transporter  | mobility/occupancy
                     |               cc: | @ C2.Transmitter | (e.g. respiratory contact)
---
          C1.Init -> | Contagion/rq      | @ C1.Transmitter | pressure (space, locus, vector, route, ...)
     Contagion/ex -> | Infection/rq      | @ P1.Afflictor   | invasions (pressure, immunity, morbidity)
---
          D1.Init -> | Adjustment/rq     | @ D1.Adjuster    | demography (life span, households, sentiment, employment)
    Adjustment/ex -> | Disruption/rq     | @ P1.Disruptor   | vitae (birth/death, partner/separate, leave/move home)
---


#### Vaccination (Mental + Medical dynamics)

(a)       I1.Init -> Information/rq    @ I1.Informer    : broadcasts (outbreak/treatment effects)
   Information/ex -> Motivation/rq     @ P1.Motivator   : impressions (reported)
(b)   Activity/ex -> Contact/rq        @ P2.Mixer       : relations (peer group, channel/medium, )
                          internal cc: @ P1.Motivator   : (e.g. observed impressions)
(c)  Contagion/ex -> Infection/rq      @ P1.Afflictor   : invasions (pressure vs immunity)
                          internal cc: @ P1.Motivator   : (e.g. experienced impressions)
(d)       H1.Init -> Advice/rq         @ H1.Advisor     : invitations (extra rounds)
        Advice/ex -> Motivation/rq     @ P1.Motivator   : impressions (campaigned)
(e)       P1.Init -> Redirection/rq    @ P1.Director    : behaviors (dis/abilities, interests, beliefs)
   Redirection/ex -> Opinion/rq        @ P3.Opinionator : expressions (active consultation, offline/online)
       Opinion/ex -> Motivation/rq     @ P1.Motivator   : impressions (subjective)
