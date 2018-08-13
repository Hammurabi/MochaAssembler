/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Riverssen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.riverssen.core.gpu;

import com.riverssen.core.system.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.*;

import static org.lwjgl.opencl.CL10.*;

public class GPUMiningInstance
{
    static final String source =
            "sha1_kernel void sum(global const float *a, global const float *b, global float *answer) { "
                    + "  unsigned int xid = get_global_id(0); "
                    + "  answer[xid] = a[xid] + b[xid];"
                    + "}";

    private CLContext context;
    private CLPlatform platform;
    private List<CLDevice> gpuDevices;
    private List<CLDevice> cpuDevices;
    private List<CLCommandQueue> commandQueues;
    private List<CLKernel>       gpuKernels;

    private GPUMiningInstance(CLContext clContext, CLPlatform platform, List<CLDevice> gpuDevices, List<CLDevice> cpuDevices, List<CLCommandQueue> commandQueues)
    {
        this.context = clContext;
        this.platform = platform;
        this.gpuDevices = gpuDevices;
        this.cpuDevices = cpuDevices;
        this.commandQueues = commandQueues;

        buffer_keys_sha1    = clCreateBuffer(context, CL_MEM_READ_ONLY, SHA1_PLAINTEXT_LENGTH_SHA1 * 4, null);
        buffer_out_sha1     = clCreateBuffer(context, CL_MEM_WRITE_ONLY, 4 * SHA1_RESULT_SIZE_SHA1, null);
        data_info_sha1      = clCreateBuffer(context, CL_MEM_READ_ONLY, 4 * 3, null);
        buffer_keys_sha256  = clCreateBuffer(context, CL_MEM_READ_ONLY, SHA256_PLAINTEXT_LENGTH * 4, null);
        buffer_out_sha256   = clCreateBuffer(context, CL_MEM_WRITE_ONLY, 4 * SHA256_RESULT_SIZE, null);
        data_info_sha256    = clCreateBuffer(context, CL_MEM_READ_ONLY, 4 * 3, null);

        sha1_program        = clCreateProgramWithSource(context, GPUMiningInstance.sha1, null);

        int error           = clBuildProgram(sha1_program, gpuDevices.get(0), "", null);

        sha1_kernel         = clCreateKernel(sha1_program, "sha1_crypt_kernel", null);

        Util.checkCLError(error);

        sha1_kernel.setArg(0, data_info_sha1);
        sha1_kernel.setArg(1, buffer_keys_sha1);
        sha1_kernel.setArg(2, buffer_out_sha1);

        sha256_program        = clCreateProgramWithSource(context, GPUMiningInstance.sha256, null);

        error           = clBuildProgram(sha256_program, gpuDevices.get(0), "", null);

        sha256_kernel         = clCreateKernel(sha256_program, "sha256_crypt_kernel", null);

        Util.checkCLError(error);

        sha256_kernel.setArg(0, data_info_sha256);
        sha256_kernel.setArg(1, buffer_keys_sha256);
        sha256_kernel.setArg(2, buffer_out_sha256);

        nonce_program        = clCreateProgramWithSource(context, GPUMiningInstance.sha256_, null);

        error                = clBuildProgram(sha256_program, gpuDevices.get(0), "", null);

//        nonce_kernel         = clCreateKernel(sha256_program, "sha256_crypt_kernel_", null);
//
//        Util.checkCLError(error);
//
//        nonce_info_sha256    = clCreateBuffer(context, CL_MEM_READ_ONLY, 2 * 8, null);
    }

    // Data buffers to store the input and result data in

    /** Utility method to convert float array to float buffer
     * @param floats - the float array to convert
     * @return a float buffer containing the input float array
     */
    static FloatBuffer toFloatBuffer(float[] floats) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(floats.length).put(floats);
        buf.rewind();
        return buf;
    }


    /** Utility method to print a float buffer
     * @param buffer - the float buffer to print to System.out
     */
    static void print(FloatBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.print(buffer.get(i)+" ");
        }
        System.out.println("");
    }

    private static String loadProgram()
    {
        try {
            String program = "";

            BufferedReader reader = new BufferedReader(new FileReader(new File("")));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return "";
    }

    final static String sha1 = "/* \n" +
            "   This code was largely inspired by \n" +
            "   pyrit opencl sha1_kernel sha1 routines.\n" +
            "   Copyright 2011 by chucheng \n" +
            "   zunceng at gmail dot com\n" +
            "   This sha1_program comes with ABSOLUTELY NO WARRANTY; express or\n" +
            "   implied .\n" +
            "   This is free software, and you are welcome to redistribute it\n" +
            "   under certain conditions; as expressed here \n" +
            "   http://www.gnu.org/licenses/gpl-2.0.html\n" +
            "*/\n" +
            "\n" +
            "#ifdef cl_khr_byte_addressable_store\n" +
            "#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : disable\n" +
            "#endif\n" +
            "\n" +
            "#ifdef cl_nv_pragma_unroll\n" +
            "#define NVIDIA\n" +
            "#pragma OPENCL EXTENSION cl_nv_pragma_unroll : enable\n" +
            "#endif\n" +
            "\n" +
            "#ifdef NVIDIA\n" +
            "inline uint SWAP32(uint x)\n" +
            "{\n" +
            "\tx = rotate(x, 16U);\n" +
            "\treturn ((x & 0x00FF00FF) << 8) + ((x >> 8) & 0x00FF00FF);\n" +
            "}\n" +
            "#else\n" +
            "#define SWAP32(a)\t(as_uint(as_uchar4(a).wzyx))\n" +
            "#endif\n" +
            "\n" +
            "#define K0  0x5A827999\n" +
            "#define K1  0x6ED9EBA1\n" +
            "#define K2  0x8F1BBCDC\n" +
            "#define K3  0xCA62C1D6\n" +
            "\n" +
            "#define H1 0x67452301\n" +
            "#define H2 0xEFCDAB89\n" +
            "#define H3 0x98BADCFE\n" +
            "#define H4 0x10325476\n" +
            "#define H5 0xC3D2E1F0\n" +
            "\n" +
            "#ifndef uint32_t\n" +
            "#define uint32_t unsigned int\n" +
            "#endif\n" +
            "\n" +
            "uint32_t SHA1CircularShift(int bits, uint32_t word)\n" +
            "{\n" +
            "\treturn ((word << bits) & 0xFFFFFFFF) | (word) >> (32 - (bits));\n" +
            "}\n" +
            "\n" +
            "__kernel void sha1_crypt_kernel(__global uint *data_info_sha1,__global char *plain_key,  __global uint *digest){\n" +
            "    int t, gid, msg_pad;\n" +
            "    int stop, mmod;\n" +
            "    uint i, ulen, item, total;\n" +
            "    uint W[80], temp, A,B,C,D,E;\n" +
            "    uint num_keys = data_info_sha1[1];\n" +
            "\tint current_pad;\n" +
            "\n" +
            "\tmsg_pad=0;\n" +
            "\n" +
            "\tulen = data_info_sha1[2];\n" +
            "\ttotal = ulen%64>=56?2:1 + ulen/64;\n" +
            "\n" +
            "\t//printf(\"ulen: %u total:%u\\n\", ulen, total);\n" +
            "\n" +
            "    digest[0] = 0x67452301;\n" +
            "\tdigest[1] = 0xEFCDAB89;\n" +
            "\tdigest[2] = 0x98BADCFE;\n" +
            "\tdigest[3] = 0x10325476;\n" +
            "\tdigest[4] = 0xC3D2E1F0;\n" +
            "\tfor(item=0; item<total; item++)\n" +
            "\t{\n" +
            "\n" +
            "\t\tA = digest[0];\n" +
            "\t\tB = digest[1];\n" +
            "\t\tC = digest[2];\n" +
            "\t\tD = digest[3];\n" +
            "\t\tE = digest[4];\n" +
            "\n" +
            "\t#pragma unroll\n" +
            "\t\tfor (t = 0; t < 80; t++){\n" +
            "\t\tW[t] = 0x00000000;\n" +
            "\t\t}\n" +
            "\t\tmsg_pad=item*64;\n" +
            "\t\tif(ulen > msg_pad)\n" +
            "\t\t{\n" +
            "\t\t\tcurrent_pad = (ulen-msg_pad)>64?64:(ulen-msg_pad);\n" +
            "\t\t}\n" +
            "\t\telse\n" +
            "\t\t{\n" +
            "\t\t\tcurrent_pad =-1;\t\t\n" +
            "\t\t}\n" +
            "\n" +
            "\t\t//printf(\"current_pad: %d\\n\",current_pad);\n" +
            "\t\tif(current_pad>0)\n" +
            "\t\t{\n" +
            "\t\t\ti=current_pad;\n" +
            "\n" +
            "\t\t\tstop =  i/4;\n" +
            "\t\t\t//printf(\"i:%d, stop: %d msg_pad:%d\\n\",i,stop, msg_pad);\n" +
            "\t\t\tfor (t = 0 ; t < stop ; t++){\n" +
            "\t\t\t\tW[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "\t\t\t\tW[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;\n" +
            "\t\t\t\tW[t] |= ((uchar) plain_key[msg_pad + t * 4 + 2]) << 8;\n" +
            "\t\t\t\tW[t] |= (uchar)  plain_key[msg_pad + t * 4 + 3];\n" +
            "\t\t\t\t//printf(\"W[%u]: %u\\n\",t,W[t]);\n" +
            "\t\t\t}\n" +
            "\t\t\tmmod = i % 4;\n" +
            "\t\t\tif ( mmod == 3){\n" +
            "\t\t\t\tW[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "\t\t\t\tW[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;\n" +
            "\t\t\t\tW[t] |= ((uchar) plain_key[msg_pad + t * 4 + 2]) << 8;\n" +
            "\t\t\t\tW[t] |=  ((uchar) 0x80) ;\n" +
            "\t\t\t} else if (mmod == 2) {\n" +
            "\t\t\t\tW[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "\t\t\t\tW[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;\n" +
            "\t\t\t\tW[t] |=  0x8000 ;\n" +
            "\t\t\t} else if (mmod == 1) {\n" +
            "\t\t\t\tW[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "\t\t\t\tW[t] |=  0x800000 ;\n" +
            "\t\t\t} else /*if (mmod == 0)*/ {\n" +
            "\t\t\t\tW[t] =  0x80000000 ;\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\tif (current_pad<56)\n" +
            "\t\t\t{\n" +
            "\t\t\t\tW[15] =  ulen*8 ;\n" +
            "\t\t\t\t//printf(\"w[15] :%u\\n\", W[15]);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\telse if(current_pad <0)\n" +
            "\t\t{\n" +
            "\t\t\tif( ulen%64==0)\n" +
            "\t\t\t\tW[0]=0x80000000;\n" +
            "\t\t\tW[15]=ulen*8;\n" +
            "\t\t\t//printf(\"w[15] :%u\\n\", W[15]);\n" +
            "\t\t}\n" +
            "\n" +
            "\t\t\n" +
            "\n" +
            "\t\tfor (t = 16; t < 80; t++)\n" +
            "\t\t{\n" +
            "\t\t\tW[t] = SHA1CircularShift(1, W[t - 3] ^ W[t - 8] ^ W[t - 14] ^ W[t - 16]);\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tfor (t = 0; t < 20; t++)\n" +
            "\t\t{\n" +
            "\t\t\ttemp = SHA1CircularShift(5, A) +\n" +
            "\t\t\t\t((B & C) | ((~B) & D)) + E + W[t] + K0;\n" +
            "\t\t\ttemp &= 0xFFFFFFFF;\n" +
            "\t\t\tE = D;\n" +
            "\t\t\tD = C;\n" +
            "\t\t\tC = SHA1CircularShift(30, B);\n" +
            "\t\t\tB = A;\n" +
            "\t\t\tA = temp;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tfor (t = 20; t < 40; t++)\n" +
            "\t\t{\n" +
            "\t\t\ttemp = SHA1CircularShift(5, A) + (B ^ C ^ D) + E + W[t] + K1;\n" +
            "\t\t\ttemp &= 0xFFFFFFFF;\n" +
            "\t\t\tE = D;\n" +
            "\t\t\tD = C;\n" +
            "\t\t\tC = SHA1CircularShift(30, B);\n" +
            "\t\t\tB = A;\n" +
            "\t\t\tA = temp;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tfor (t = 40; t < 60; t++)\n" +
            "\t\t{\n" +
            "\t\t\ttemp = SHA1CircularShift(5, A) +\n" +
            "\t\t\t\t((B & C) | (B & D) | (C & D)) + E + W[t] + K2;\n" +
            "\t\t\ttemp &= 0xFFFFFFFF;\n" +
            "\t\t\tE = D;\n" +
            "\t\t\tD = C;\n" +
            "\t\t\tC = SHA1CircularShift(30, B);\n" +
            "\t\t\tB = A;\n" +
            "\t\t\tA = temp;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tfor (t = 60; t < 80; t++)\n" +
            "\t\t{\n" +
            "\t\t\ttemp = SHA1CircularShift(5, A) + (B ^ C ^ D) + E + W[t] + K3;\n" +
            "\t\t\ttemp &= 0xFFFFFFFF;\n" +
            "\t\t\tE = D;\n" +
            "\t\t\tD = C;\n" +
            "\t\t\tC = SHA1CircularShift(30, B);\n" +
            "\t\t\tB = A;\n" +
            "\t\t\tA = temp;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tdigest[0] = (digest[0] + A) & 0xFFFFFFFF;\n" +
            "\t\tdigest[1] = (digest[1] + B) & 0xFFFFFFFF;\n" +
            "\t\tdigest[2] = (digest[2] + C) & 0xFFFFFFFF;\n" +
            "\t\tdigest[3] = (digest[3] + D) & 0xFFFFFFFF;\n" +
            "\t\tdigest[4] = (digest[4] + E) & 0xFFFFFFFF;\n" +
            "\n" +
            "\t\t//for(i=0;i<80;i++)\n" +
            "\t\t\t//printf(\"W[%u]: %u\\n\", i,W[i] );\n" +
            "\n" +
            "\t\t//printf(\"%u\\n\",  digest[0]);\n" +
            "\t\t//printf(\"%u\\n\",  digest[1]);\n" +
            "\t\t//printf(\"%u\\n\",  digest[2]);\n" +
            "\t\t//printf(\"%u\\n\",  digest[3]);\n" +
            "\t\t//printf(\"%u\\n\",  digest[4]);\n" +
            "\t}\n" +
            "}";

    final static String sha256 = "#ifndef uint32_t\n" +
            "#define uint32_t unsigned int\n" +
            "#endif\n" +
            "\n" +
            "#define H0 0x6a09e667\n" +
            "#define H1 0xbb67ae85\n" +
            "#define H2 0x3c6ef372\n" +
            "#define H3 0xa54ff53a\n" +
            "#define H4 0x510e527f\n" +
            "#define H5 0x9b05688c\n" +
            "#define H6 0x1f83d9ab\n" +
            "#define H7 0x5be0cd19\n" +
            "\n" +
            "\n" +
            "uint rotr(uint x, int n) {\n" +
            "  if (n < 32) return (x >> n) | (x << (32 - n));\n" +
            "  return x;\n" +
            "}\n" +
            "\n" +
            "uint ch(uint x, uint y, uint z) {\n" +
            "  return (x & y) ^ (~x & z);\n" +
            "}\n" +
            "\n" +
            "uint maj(uint x, uint y, uint z) {\n" +
            "  return (x & y) ^ (x & z) ^ (y & z);\n" +
            "}\n" +
            "\n" +
            "uint sigma0(uint x) {\n" +
            "  return rotr(x, 2) ^ rotr(x, 13) ^ rotr(x, 22);\n" +
            "}\n" +
            "\n" +
            "uint sigma1(uint x) {\n" +
            "  return rotr(x, 6) ^ rotr(x, 11) ^ rotr(x, 25);\n" +
            "}\n" +
            "\n" +
            "uint gamma0(uint x) {\n" +
            "  return rotr(x, 7) ^ rotr(x, 18) ^ (x >> 3);\n" +
            "}\n" +
            "\n" +
            "uint gamma1(uint x) {\n" +
            "  return rotr(x, 17) ^ rotr(x, 19) ^ (x >> 10);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "__kernel void sha256_crypt_kernel(__global uint *data_info_sha1,__global char *plain_key,  __global uint *digest){\n" +
            "  int t, gid, msg_pad;\n" +
            "  int stop, mmod;\n" +
            "  uint i, ulen, item, total;\n" +
            "  uint W[80], temp, A,B,C,D,E,F,G,H,T1,T2;\n" +
            "  uint num_keys = data_info_sha1[1];\n" +
            "  int current_pad;\n" +
            "\n" +
            "  uint K[64]={\n" +
            "0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,\n" +
            "0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,\n" +
            "0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,\n" +
            "0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,\n" +
            "0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,\n" +
            "0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,\n" +
            "0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,\n" +
            "0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2\n" +
            "};\n" +
            "\n" +
            "  msg_pad=0;\n" +
            "\n" +
            "  ulen = data_info_sha1[2];\n" +
            "  total = ulen%64>=56?2:1 + ulen/64;\n" +
            "\n" +
            "//  printf(\"ulen: %u total:%u\\n\", ulen, total);\n" +
            "\n" +
            "  digest[0] = H0;\n" +
            "  digest[1] = H1;\n" +
            "  digest[2] = H2;\n" +
            "  digest[3] = H3;\n" +
            "  digest[4] = H4;\n" +
            "  digest[5] = H5;\n" +
            "  digest[6] = H6;\n" +
            "  digest[7] = H7;\n" +
            "  for(item=0; item<total; item++)\n" +
            "  {\n" +
            "\n" +
            "    A = digest[0];\n" +
            "    B = digest[1];\n" +
            "    C = digest[2];\n" +
            "    D = digest[3];\n" +
            "    E = digest[4];\n" +
            "    F = digest[5];\n" +
            "    G = digest[6];\n" +
            "    H = digest[7];\n" +
            "\n" +
            "#pragma unroll\n" +
            "    for (t = 0; t < 80; t++){\n" +
            "    W[t] = 0x00000000;\n" +
            "    }\n" +
            "    msg_pad=item*64;\n" +
            "    if(ulen > msg_pad)\n" +
            "    {\n" +
            "      current_pad = (ulen-msg_pad)>64?64:(ulen-msg_pad);\n" +
            "    }\n" +
            "    else\n" +
            "    {\n" +
            "      current_pad =-1;    \n" +
            "    }\n" +
            "\n" +
            "  //  printf(\"current_pad: %d\\n\",current_pad);\n" +
            "    if(current_pad>0)\n" +
            "    {\n" +
            "      i=current_pad;\n" +
            "\n" +
            "      stop =  i/4;\n" +
            "  //    printf(\"i:%d, stop: %d msg_pad:%d\\n\",i,stop, msg_pad);\n" +
            "      for (t = 0 ; t < stop ; t++){\n" +
            "        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;\n" +
            "        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 2]) << 8;\n" +
            "        W[t] |= (uchar)  plain_key[msg_pad + t * 4 + 3];\n" +
            "        //printf(\"W[%u]: %u\\n\",t,W[t]);\n" +
            "      }\n" +
            "      mmod = i % 4;\n" +
            "      if ( mmod == 3){\n" +
            "        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;\n" +
            "        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 2]) << 8;\n" +
            "        W[t] |=  ((uchar) 0x80) ;\n" +
            "      } else if (mmod == 2) {\n" +
            "        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;\n" +
            "        W[t] |=  0x8000 ;\n" +
            "      } else if (mmod == 1) {\n" +
            "        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;\n" +
            "        W[t] |=  0x800000 ;\n" +
            "      } else /*if (mmod == 0)*/ {\n" +
            "        W[t] =  0x80000000 ;\n" +
            "      }\n" +
            "      \n" +
            "      if (current_pad<56)\n" +
            "      {\n" +
            "        W[15] =  ulen*8 ;\n" +
            "        //printf(\"ulen avlue 2 :w[15] :%u\\n\", W[15]);\n" +
            "      }\n" +
            "    }\n" +
            "    else if(current_pad <0)\n" +
            "    {\n" +
            "      if( ulen%64==0)\n" +
            "        W[0]=0x80000000;\n" +
            "      W[15]=ulen*8;\n" +
            "      //printf(\"ulen avlue 3 :w[15] :%u\\n\", W[15]);\n" +
            "    }\n" +
            "\n" +
            "    for (t = 0; t < 64; t++) {\n" +
            "      if (t >= 16)\n" +
            "        W[t] = gamma1(W[t - 2]) + W[t - 7] + gamma0(W[t - 15]) + W[t - 16];\n" +
            "      T1 = H + sigma1(E) + ch(E, F, G) + K[t] + W[t];\n" +
            "      T2 = sigma0(A) + maj(A, B, C);\n" +
            "      H = G; G = F; F = E; E = D + T1; D = C; C = B; B = A; A = T1 + T2;\n" +
            "    }\n" +
            "    digest[0] += A;\n" +
            "    digest[1] += B;\n" +
            "    digest[2] += C;\n" +
            "    digest[3] += D;\n" +
            "    digest[4] += E;\n" +
            "    digest[5] += F;\n" +
            "    digest[6] += G;\n" +
            "    digest[7] += H;\n" +
            "\n" +
            "  //  for (t = 0; t < 80; t++)\n" +
            "  //    {\n" +
            "  //    printf(\"W[%d]: %u\\n\",t,W[t]);\n" +
            "  //    }\n" +
            "  }\n" +
            "\n" +
            "\n" +
            "}";

    final static String sha256_ = "";

    final static String program = "" +
            "sha1_kernel void entryPoint(global const char* block, const long start, const long end, global char* result)" +
            "{" +
            "   if(get_global_id(0) >= (get_global_size(0) - 1))" +
            "{" +
            "   result[0] = get_global_id(0);" +
            "   result[1] = 2;" +
            "}" +
            "}" +
            "";

    public String solveBlock(ByteBuffer block, int device)
    {
        int string_len = block.capacity() + 4;
        int global_work_size = 1;
        IntBuffer datai = BufferUtils.createIntBuffer(3);

        datai.put(SHA256_PLAINTEXT_LENGTH);
        datai.put(global_work_size);
        datai.put(string_len);
        datai.flip();

//        ByteBuffer saved_plain = BufferUtils.createByteBuffer(string_len);
        PointerBuffer  local_work_size = BufferUtils.createPointerBuffer(1);
        local_work_size.put(1);
        local_work_size.flip();

        ByteBuffer saved_plain[] = new ByteBuffer[256];

        for(int i = 0; i < 256; i ++)
        {
            saved_plain[i] = BufferUtils.createByteBuffer(string_len);

            saved_plain[i].put(block);
            saved_plain[i].putInt(0);

            saved_plain[i].flip();
        }

        IntBuffer partial_hashes = BufferUtils.createIntBuffer(SHA256_RESULT_SIZE);

        CLCommandQueue command_queue = commandQueues.get(device);

        for(long i = 0; i < 256; i ++)
        {
            int ret = 0;
            clEnqueueWriteBuffer(command_queue, data_info_sha256, CL_TRUE, 0, datai, null, null);
            clEnqueueWriteBuffer(command_queue, buffer_keys_sha256, CL_TRUE, 0, saved_plain[(int) i], null, null);
            clEnqueueNDRangeKernel(commandQueues.get(device), sha256_kernel, 1, null, local_work_size, null, null, null);


            ret = clFinish(command_queue);
//            clEnqueueReadBuffer(commandQueues.get(device), nonce_info_sha256, CL_TRUE, 0, partial_hashes, null, null);
        }

        String result = "";

//        for(int i=0; i < SHA256_RESULT_SIZE; i++)
//            result += String.format("%08x", partial_hashes.get(i));

        return result;
    }

    final static int SHA1_PLAINTEXT_LENGTH_SHA1 = 64;
    final static int SHA1_BINARY_SIZE_SHA1      = 20;
    final static int SHA1_RESULT_SIZE_SHA1      = 5;

    final static int SHA256_PLAINTEXT_LENGTH    = 64;
    final static int SHA256_BINARY_SIZE         = 32;
    final static int SHA256_RESULT_SIZE         = 8;

    final CLMem buffer_keys_sha1;
    final CLMem buffer_out_sha1;
    final CLMem data_info_sha1;
    final CLMem buffer_keys_sha256;
    final CLMem buffer_out_sha256;
//    final CLMem nonce_info_sha256;
    final CLMem data_info_sha256;

    final CLKernel sha1_kernel;
    final CLProgram sha1_program;

    final CLKernel sha256_kernel;
    final CLProgram sha256_program;
//    final CLKernel nonce_kernel;
    final CLProgram nonce_program;
//    final static IntBuffer datai = BufferUtils.createIntBuffer(3);

    private IntBuffer wrap(int i)
    {
        IntBuffer buffer = BufferUtils.createIntBuffer(1);

        buffer.put(i);

        buffer.flip();

        return buffer;
    }

    public String sha1_crypt(String input, int device)
    {
        int i;
        int
                string_len = (input).length();
        int global_work_size = 1;
        IntBuffer datai = BufferUtils.createIntBuffer(3);

        datai.put(SHA1_PLAINTEXT_LENGTH_SHA1);
        datai.put(global_work_size);
        datai.put(string_len);

        datai.flip();

        ByteBuffer saved_plain = BufferUtils.createByteBuffer(string_len);
        PointerBuffer  local_work_size = BufferUtils.createPointerBuffer(1);
        local_work_size.put(1);
        local_work_size.flip();

        saved_plain.put(input.getBytes());
        saved_plain.flip();

        IntBuffer partial_hashes = BufferUtils.createIntBuffer(SHA1_RESULT_SIZE_SHA1);

        CLCommandQueue command_queue = commandQueues.get(device);

        {
            //printf("%s\n",saved_plain);
            int ret = 0;
//            ret = clEnqueueWriteBuffer(command_queue, data_info_sha1, CL_TRUE, 0, 4 * 3, datai, 0, null, null);
            clEnqueueWriteBuffer(command_queue, data_info_sha1, CL_TRUE, 0, datai, null, null);
            clEnqueueWriteBuffer(command_queue, buffer_keys_sha1, CL_TRUE, 0, saved_plain, null, null);
//            ret = clEnqueueWriteBuffer(command_queue, buffer_keys_sha1, CL_TRUE, 0, SHA1_PLAINTEXT_LENGTH_SHA1 * 4, saved_plain, 0, null, null);
//            printf("%s\n",buffer_keys_sha1);
//            ret = clEnqueueNDRangeKernel(command_queue, sha1_kernel, 1, null, global_work_size, local_work_size, 0, null, null);
            clEnqueueNDRangeKernel(commandQueues.get(device), sha1_kernel, 1, null, local_work_size, null, null, null);


            ret = clFinish(command_queue);
            // read back partial hashes
            clEnqueueReadBuffer(commandQueues.get(device), buffer_out_sha1, CL_TRUE, 0, partial_hashes, null, null);
//            ret = clEnqueueReadBuffer(command_queue, buffer_out_sha1, CL_TRUE, 0, 4 * SHA1_RESULT_SIZE_SHA1, partial_hashes, 0, null, null);
//            have_full_hashes = 0;
        }

        String result = "";

        for(i=0; i < SHA1_RESULT_SIZE_SHA1; i++)
            result += String.format("%08x", partial_hashes.get(i));

        return result;
    }

    public String sha256_crypt(String input, int device)
    {
        int i;
        int
                string_len = (input).length();
        int global_work_size = 1;
        IntBuffer datai = BufferUtils.createIntBuffer(3);

        datai.put(SHA256_PLAINTEXT_LENGTH);
        datai.put(global_work_size);
        datai.put(string_len);

        datai.flip();

        ByteBuffer saved_plain = BufferUtils.createByteBuffer(string_len);
        PointerBuffer  local_work_size = BufferUtils.createPointerBuffer(1);
        local_work_size.put(1);
        local_work_size.flip();

        saved_plain.put(input.getBytes());
        saved_plain.flip();

        IntBuffer partial_hashes = BufferUtils.createIntBuffer(SHA256_RESULT_SIZE);

        CLCommandQueue command_queue = commandQueues.get(device);

        {
            int ret = 0;
            clEnqueueWriteBuffer(command_queue, data_info_sha256, CL_TRUE, 0, datai, null, null);
            clEnqueueWriteBuffer(command_queue, buffer_keys_sha256, CL_TRUE, 0, saved_plain, null, null);
            clEnqueueNDRangeKernel(commandQueues.get(device), sha256_kernel, 1, null, local_work_size, null, null, null);


            ret = clFinish(command_queue);
            clEnqueueReadBuffer(commandQueues.get(device), buffer_out_sha256, CL_TRUE, 0, partial_hashes, null, null);
        }

        String result = "";

        for(i=0; i < SHA256_RESULT_SIZE; i++)
            result += String.format("%08x", partial_hashes.get(i));

        return result;
    }

    public void run(ByteBuffer blockData, int device, long inclusiveStart, long inclusiveEnd)
    {
        CLProgram program   = clCreateProgramWithSource(context, GPUMiningInstance.program, null);

        int error           = clBuildProgram(program, gpuDevices.get(device), "", null);

        Util.checkCLError(error);

        CLKernel kernel     = clCreateKernel(program, "entryPoint", null);

        CLMem blockDataMem  = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, blockData, null);
        CLMem resultMemory  = clCreateBuffer(context, CL_MEM_WRITE_ONLY, 16, null);

        kernel.setArg(0, blockDataMem);
        kernel.setArg(1, (long)inclusiveStart);
        kernel.setArg(2, (long)inclusiveStart);
        kernel.setArg(3, resultMemory);

        PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(1);
        globalWorkSize.put(0, inclusiveStart + inclusiveEnd + 1);

        clEnqueueNDRangeKernel(commandQueues.get(device), kernel, 1, null, globalWorkSize, null, null, null);

        clFinish(commandQueues.get(device));

        LongBuffer result   = BufferUtils.createLongBuffer(2);

        clEnqueueReadBuffer(commandQueues.get(device), resultMemory, CL_TRUE, 0, result, null, null);

        System.out.println(Long.toUnsignedString(result.get(0)) + " " + Long.toUnsignedString(result.get(1)));

//        clReleaseProgram(sha1_program);
//        clReleaseKernel(sha1_kernel);
//        clReleaseMemObject(blockDataMem);
//        clReleaseMemObject(resultMemory);
    }

    public synchronized ByteBuffer createCharPointer(int size)
    {
        return BufferUtils.createByteBuffer(size);
    }

    public void destroy()
    {
        clReleaseMemObject(buffer_keys_sha1);
        clReleaseMemObject(buffer_out_sha1);
        clReleaseMemObject(data_info_sha1);

        clReleaseKernel(sha1_kernel);
        clReleaseProgram(sha1_program);

        CL.destroy();
    }

    static GPUMiningInstance instance;

    static {
        try
        {
//            datai.put(0);
//            datai.put(0);
//            datai.put(0);
//            datai.flip();
            CL.create();

            CLPlatform platform = CLPlatform.getPlatforms().get(0);

            List<CLDevice> gpuDevices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
            List<CLDevice> cpuDevices = platform.getDevices(CL10.CL_DEVICE_TYPE_CPU);

            if(gpuDevices == null) gpuDevices = new ArrayList<>();
            if(cpuDevices == null) cpuDevices = new ArrayList<>();

            Logger.alert("--------------------------");
            Logger.alert("GPUMiningInstance created:");
            Logger.alert("    GPUdevices found: " + gpuDevices.size());
            for(CLDevice device : gpuDevices)
                Logger.alert("        " + device.getInfoString(CL10.CL_DEVICE_NAME));
            Logger.alert("    CPUdevices found: " + cpuDevices.size() + "\n");
            for(CLDevice device : cpuDevices)
                Logger.alert("        " + device.getInfoString(CL10.CL_DEVICE_NAME));
            Logger.alert("--------------------------");

            CLContext clContext = CLContext.create(platform, gpuDevices, null);

            List<CLCommandQueue> commandQueues = new ArrayList<>();

            for(CLDevice device : gpuDevices)
            {
                IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
                CLCommandQueue queue = clCreateCommandQueue(clContext, device, CL_QUEUE_PROFILING_ENABLE, errorBuffer);
                Util.checkCLError(errorBuffer.get(0));

                commandQueues.add(queue);
            }

            instance = new GPUMiningInstance(clContext, platform, gpuDevices, cpuDevices, commandQueues);
        } catch (LWJGLException e)
        {
            e.printStackTrace();
//            RivercoinCore.setGPUMining(false);
        }
    }

    public static GPUMiningInstance getInstance()
    {
        return instance;
    }

    public static ByteBuffer wrap(byte data[])
    {
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);

        buffer.put(data);

        buffer.flip();
        return buffer;
    }
}