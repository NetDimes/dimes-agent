# MAkefile statement to compile the JavaNetStack shared object :
all: 
	g++ -g -I ./include/ -I ./include/linux/ *.cpp -shared -o Debug/NetworkStack.so

clean:
	rm -v Debug/*.so

# a make utility for the etomic platform :	
etomic:
	g++ -shared -I ./include/ -I ./include/linux/ -I /usr/local/dag/include \
	-DDIMES_ETOMIC  /usr/local/dag/lib/libeverdag.a *.cpp \
	-lpcap -lnet -o Debug/NetworkStack.so	
	
	

