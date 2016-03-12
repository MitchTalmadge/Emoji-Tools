/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 - 2016 Mitch Talmadge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact Mitch Talmadge at mitcht@liveforcode.net
 */

package com.mitchtalmadge.emojitools.operations.conversion.converter;

public class PNGFilterHandler {

    /**
     * filter type
     */
    public final static int FILTER_NONE = 0;
    public final static int FILTER_SUB = 1;
    public final static int FILTER_UP = 2;
    public final static int FILTER_AVG = 3;
    public final static int FILTER_PAETH = 4;

    /**
     * Unfilter a PNG image row.
     *
     * @param filterType filter type for this row
     * @param pixels     pixels array of the image
     * @param y          line number to unfilter
     * @param width      width of the row
     */
    public static void filter(int filterType, byte pixels[], int y, int width, boolean compress, boolean alpha) {
        int rowOffset = y * width;
        /**
         * Perform filtering if any
         */
        switch (filterType) {
            case FILTER_NONE:
                break;
            case FILTER_SUB: {
                int AA = 0;
                int RA = 0;
                int GA = 0;
                int BA = 0;
                for (int n = 1; n < width; n += alpha ? 4 : 3) {
                    int R = pixels[rowOffset + n] & 0xFF;
                    int G = pixels[rowOffset + n + 1] & 0xFF;
                    int B = pixels[rowOffset + n + 2] & 0xFF;
                    int A = 0;
                    if (alpha)
                        A = pixels[rowOffset + n + 3] & 0xFF;

                    if (compress) {
                        A -= AA;
                        R -= RA;
                        G -= GA;
                        B -= BA;
                    } else {
                        A += AA;
                        R += RA;
                        G += GA;
                        B += BA;
                    }

                    A &= 0xFF;
                    R &= 0xFF;
                    G &= 0xFF;
                    B &= 0xFF;

                    pixels[rowOffset + n] = (byte) R;
                    pixels[rowOffset + n + 1] = (byte) G;
                    pixels[rowOffset + n + 2] = (byte) B;
                    if (alpha)
                        pixels[rowOffset + n + 3] = (byte) A;

                    RA = R;
                    GA = G;
                    BA = B;
                    AA = A;
                }
            }
            break;
            case FILTER_UP: {
                int priorRowOffset = rowOffset - width;
                for (int n = 1; n < width; n += alpha ? 4 : 3) {

                    int RB = 0, GB = 0, BB = 0, AB = 0;
                    if (y > 0) {
                        RB = pixels[priorRowOffset + n] & 0xFF;
                        GB = pixels[priorRowOffset + n + 1] & 0xFF;
                        BB = pixels[priorRowOffset + n + 2] & 0xFF;
                        if (alpha)
                            AB = pixels[priorRowOffset + n + 3] & 0xFF;
                    }

                    int R = pixels[rowOffset + n] & 0xFF;
                    int G = pixels[rowOffset + n + 1] & 0xFF;
                    int B = pixels[rowOffset + n + 2] & 0xFF;
                    int A = 0;
                    if (alpha)
                        A = pixels[rowOffset + n + 3] & 0xFF;

                    if (compress) {
                        A -= AB;
                        R -= RB;
                        G -= GB;
                        B -= BB;
                    } else {
                        A += AB;
                        R += RB;
                        G += GB;
                        B += BB;
                    }

                    A &= 0xFF;
                    R &= 0xFF;
                    G &= 0xFF;
                    B &= 0xFF;

                    pixels[rowOffset + n] = (byte) R;
                    pixels[rowOffset + n + 1] = (byte) G;
                    pixels[rowOffset + n + 2] = (byte) B;
                    if (alpha)
                        pixels[rowOffset + n + 3] = (byte) A;
                }
            }
            break;
            case FILTER_AVG: {
                int AA = 0;
                int RA = 0;
                int GA = 0;
                int BA = 0;

                int priorRowOffset = rowOffset - width;
                for (int n = 1; n < width; n += alpha ? 4 : 3) {
                    int RB = 0, GB = 0, BB = 0, AB = 0;
                    if (y > 0) {
                        RB = pixels[priorRowOffset + n] & 0xFF;
                        GB = pixels[priorRowOffset + n + 1] & 0xFF;
                        BB = pixels[priorRowOffset + n + 2] & 0xFF;
                        if (alpha)
                            AB = pixels[priorRowOffset + n + 3] & 0xFF;
                    }

                    int R = pixels[rowOffset + n] & 0xFF;
                    int G = pixels[rowOffset + n + 1] & 0xFF;
                    int B = pixels[rowOffset + n + 2] & 0xFF;
                    int A = 0;
                    if (alpha)
                        A = pixels[rowOffset + n + 3] & 0xFF;

                    if (compress) {
                        A -= Math.floor(AB + AA);
                        R -= Math.floor(RB + RA);
                        G -= Math.floor(GB + GA);
                        B -= Math.floor(BB + BA);
                    } else {
                        A += Math.floor(AB + AA);
                        R += Math.floor(RB + RA);
                        G += Math.floor(GB + GA);
                        B += Math.floor(BB + BA);
                    }

                    A &= 0xFF;
                    R &= 0xFF;
                    G &= 0xFF;
                    B &= 0xFF;

                    pixels[rowOffset + n] = (byte) R;
                    pixels[rowOffset + n + 1] = (byte) G;
                    pixels[rowOffset + n + 2] = (byte) B;
                    if (alpha)
                        pixels[rowOffset + n + 3] = (byte) A;

                    AA = A;
                    RA = R;
                    GA = G;
                    BA = B;
                }
            }
            break;
            case FILTER_PAETH: {
                int RA = 0;
                int GA = 0;
                int BA = 0;
                int AA = 0;

                int RC = 0;
                int GC = 0;
                int BC = 0;
                int AC = 0;

                int priorRowOffset = rowOffset - width;
                for (int n = 1; n < width; n += alpha ? 4 : 3) {
                    int RB = 0, GB = 0, BB = 0, AB = 0;
                    if (y > 0) {
                        RB = pixels[priorRowOffset + n] & 0xFF;
                        GB = pixels[priorRowOffset + n + 1] & 0xFF;
                        BB = pixels[priorRowOffset + n + 2] & 0xFF;
                        if (alpha)
                            AB = pixels[priorRowOffset + n + 3] & 0xFF;
                    }

                    int R = pixels[rowOffset + n] & 0xFF;
                    int G = pixels[rowOffset + n + 1] & 0xFF;
                    int B = pixels[rowOffset + n + 2] & 0xFF;
                    int A = 0;
                    if (alpha)
                        A = pixels[rowOffset + n + 3] & 0xFF;

                    int PR = RA + RB - RC;
                    int PRA = Math.abs(PR - RA);
                    int PRB = Math.abs(PR - RB);
                    int PRC = Math.abs(PR - RC);
                    if (PRA <= PRB && PRA <= PRC)
                        if (compress)
                            R -= RA;
                        else
                            R += RA;
                    else {
                        if (PRB <= PRC)
                            if (compress)
                                R -= RB;
                            else
                                R += RB;
                        else if (compress)
                            R -= RC;
                        else
                            R += RC;
                    }


                    int PG = GA + GB - GC;
                    int PGA = Math.abs(PG - GA);
                    int PGB = Math.abs(PG - GB);
                    int PGC = Math.abs(PG - GC);
                    if (PGA <= PGB && PGA <= PGC)
                        if (compress)
                            G -= GA;
                        else
                            G += GA;
                    else {
                        if (PGB <= PGC)
                            if (compress)
                                G -= GB;
                            else
                                G += GB;
                        else if (compress)
                            G -= GC;
                        else
                            G += GC;
                    }

                    int PB = BA + BB - BC;
                    int PBA = Math.abs(PB - BA);
                    int PBB = Math.abs(PB - BB);
                    int PBC = Math.abs(PB - BC);
                    if (PBA <= PBB && PBA <= PBC)
                        if (compress)
                            B -= BA;
                        else
                            B += BA;
                    else {
                        if (PBB <= PBC)
                            if (compress)
                                B -= BB;
                            else
                                B += BB;
                        else if (compress)
                            B -= BC;
                        else
                            B += BC;
                    }

                    int PA = AA + AB - AC;
                    int PAA = Math.abs(PA - AA);
                    int PAB = Math.abs(PA - AB);
                    int PAC = Math.abs(PA - AC);
                    if (PAA <= PAB && PAA <= PAC)
                        if (compress)
                            A -= AA;
                        else
                            A += AA;
                    else {
                        if (PAB <= PAC)
                            if (compress)
                                A -= AB;
                            else
                                A += AB;
                        else if (compress)
                            A -= AC;
                        else
                            A += AC;
                    }

                    A &= 0xFF;
                    R &= 0xFF;
                    G &= 0xFF;
                    B &= 0xFF;

                    pixels[rowOffset + n] = (byte) R;
                    pixels[rowOffset + n + 1] = (byte) G;
                    pixels[rowOffset + n + 2] = (byte) B;
                    if (alpha)
                        pixels[rowOffset + n + 3] = (byte) A;

                    RA = R;
                    GA = G;
                    BA = B;
                    AA = A;

                    RC = RB;
                    GC = GB;
                    BC = BB;
                    AC = AB;
                }
            }
            break;
            default:
                break;
        }

    }

}
