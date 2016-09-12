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

import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link PathogenDimensionDao}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PathogenDimensionDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID", nullable = false, unique = true )
	protected int id;

	// e.g. Prokaryote, Eukaryote
	@Column( name = "DOMAIN", nullable = true, updatable = false )
	protected String domain;

	// e.g. (Eu)Bacteria [Gram negative, Gram positive], Archaea(Bacteria), Fungi, Plantae, Protista, Animalia/Metazoa
	@Column( name = "KINGDOM", nullable = true, updatable = false )
	protected String kingdom;

	// e.g. Chlamydiae, Chordata
	@Column( name = "PHYLUM", nullable = true, updatable = false )
	protected String phylum;

	// e.g. Chlamydiae, Insecta, Tetrapods
	@Column( name = "CLASS", nullable = true, updatable = false )
	protected String classis;

	// e.g. Chlamydiales, Megavirales (proposed clade), ...
	@Column( name = "ORDER", nullable = true, updatable = false )
	protected String order;

	// for virii, see Baltimore classification
	@Column( name = "GROUP", nullable = true, updatable = false )
	protected String group;

	// e.g. Chlamydiaceae, Criblamydiaceae, ...
	@Column( name = "FAMILY", nullable = true, updatable = false )
	protected String family;

	// e.g. Chlamydophila, Chlamydia, ...
	@Column( name = "GENUS", nullable = true, updatable = false )
	protected String genus;

	// e.g. Chlamydia trachomatis, Chlamydophila pneumoniae, ...
	@Column( name = "SPECIES", nullable = false, updatable = false )
	protected String species;

//	protected String type;
//
//	protected String strain;
}
