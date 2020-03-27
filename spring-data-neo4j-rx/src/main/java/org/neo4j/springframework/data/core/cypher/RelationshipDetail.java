/*
 * Copyright (c) 2019-2020 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.springframework.data.core.cypher;

import java.util.Optional;

import org.apiguardian.api.API;
import org.neo4j.springframework.data.core.cypher.Relationship.Direction;
import org.neo4j.springframework.data.core.cypher.support.Visitable;
import org.neo4j.springframework.data.core.cypher.support.Visitor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/railroad/RelationshipDetail.html">RelationshipDetail</a>
 *
 * @author Michael J. Simons
 * @author Philipp Tölle
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public final class RelationshipDetail implements Visitable {

	/**
	 * The direction between the nodes of the relationship.
	 */
	private final Direction direction;

	private @Nullable final SymbolicName symbolicName;

	private @Nullable final RelationshipTypes types;

	private @Nullable final RelationshipLength length;

	private @Nullable final Properties properties;

	static RelationshipDetail create(Direction direction, @Nullable SymbolicName symbolicName,
		@Nullable RelationshipTypes types) {

		return new RelationshipDetail(direction, symbolicName, types, null, null);
	}

	private RelationshipDetail(Direction direction,
		@Nullable SymbolicName symbolicName,
		@Nullable RelationshipTypes types,
		@Nullable RelationshipLength length,
		@Nullable Properties properties
	) {

		this.direction = direction;
		this.symbolicName = symbolicName;
		this.types = types;
		this.length = length;
		this.properties = properties;
	}

	RelationshipDetail named(String newSymbolicName) {

		Assert.hasText(newSymbolicName, "Symbolic name is required.");
		return new RelationshipDetail(this.direction, SymbolicName.create(newSymbolicName), this.types, this.length,
			this.properties);
	}

	RelationshipDetail with(@Nullable Properties newProperties) {

		return new RelationshipDetail(this.direction, this.symbolicName, this.types, this.length, newProperties);
	}

	RelationshipDetail min(Integer minimum) {

		if (minimum == null && (this.length == null || this.length.getMinimum() == null)) {
			return this;
		}

		RelationshipLength newLength = Optional.ofNullable(this.length)
			.map(l -> new RelationshipLength(minimum, l.getMaximum()))
			.orElseGet(() -> new RelationshipLength(minimum, null));

		return new RelationshipDetail(this.direction, this.symbolicName, this.types, newLength, properties);
	}

	RelationshipDetail max(Integer maximum) {

		if (maximum == null && (this.length == null || this.length.getMaximum() == null)) {
			return this;
		}

		RelationshipLength newLength = Optional.ofNullable(this.length)
			.map(l -> new RelationshipLength(l.getMinimum(), maximum))
			.orElseGet(() -> new RelationshipLength(null, maximum));

		return new RelationshipDetail(this.direction, this.symbolicName, this.types, newLength, properties);
	}

	public Direction getDirection() {
		return direction;
	}

	Optional<SymbolicName> getSymbolicName() {
		return Optional.ofNullable(symbolicName);
	}

	public RelationshipTypes getTypes() {
		return types;
	}

	@Nullable public Properties getProperties() {
		return properties;
	}

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);
		Visitable.visitIfNotNull(this.symbolicName, visitor);
		Visitable.visitIfNotNull(this.types, visitor);
		Visitable.visitIfNotNull(this.length, visitor);
		Visitable.visitIfNotNull(this.properties, visitor);
		visitor.leave(this);
	}
}
