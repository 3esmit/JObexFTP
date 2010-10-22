/*
 * Last updated in 21/Out/2010
 *
 *    This file is part of JObexFTP 2.0.
 *
 *    JObexFTP is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JObexFTP is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with JObexFTP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.lhf.obexftplib.event;

import java.util.EventListener;

/**
 * Interface of Listener who's interested of the incoming data
 * 
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public interface DataEventListener extends EventListener {

    /**
     * handler of OBEXEvent
     * 
     * @param incomingData the incoming data
     */
    public void DataEvent(byte[] event);
}
