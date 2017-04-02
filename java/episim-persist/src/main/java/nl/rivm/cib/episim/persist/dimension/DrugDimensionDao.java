/* $Id$
 * 
 * Part of ZonMW project no. 50-53000-98-156
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2016 RIVM National Institute for Health and Environment 
 */
package nl.rivm.cib.episim.persist.dimension;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import io.coala.bind.persist.LocalIdDao;
import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link DrugDimensionDao} based on nomenclature from the G-Standaard
 * (https://www.knmp.nl/producten-en-diensten/gebruiksrecht-g-standaard), and/or
 * various formularia (e.g. http://www.formularia.nl/ for Dutch general
 * practitioners, used e.g. in the farmaco-therapeutic service Prescriptor for
 * HIS solutions, or http://wvab.knmvd.nl/formularia for antibiotic
 * prescriptions by Dutch veterinarians)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class DrugDimensionDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@Column( name = "NAME", unique = true, nullable = false )
	protected String name;

//	@OneToMany
//	@JoinColumn( name = "ALIAS" )
//	protected Set<String> aliases;

	@Column( name = "CLASS", nullable = true, updatable = false )
	protected LocalIdDao clazz;
}
