/*
PresenceBasicStatus.java
Copyright (C) 2010-2013  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package chau.moudule;

/** Basic status as defined in section 4.1.4 of RFC 3863 */
public enum PresenceBasicStatus {
	/** This value means that the associated contact element, if any, is ready to accept communication. */
	Open(0),
	/** This value means that the associated contact element, if any, is unable to accept communication. */
	Closed(1),
	Invalid(2);

	protected final int mValue;

	private PresenceBasicStatus(int value) {
		mValue = value;
	}

	public int toInt() {
		return mValue;
	}

	static protected PresenceBasicStatus fromInt(int value) {
		switch (value) {
		case 0: return Open;
		case 1: return Closed;
		default: return Invalid;
		}
	}
}
