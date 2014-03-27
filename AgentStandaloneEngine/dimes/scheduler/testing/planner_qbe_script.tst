<?xml version="1.0"?>

<DIMES_experiment>
<!-- to implement: 1) channge  name 2) logicalGroup 12094 to my aigent index -->
    <generalProperties protocol="UDP" operation="TR" type="QBE" >
        <name> XML_QBE_Ex_Test_ver6_agent_11908_2007_07_02_08_25</name> 
        <description>Using an XML to create an experiment </description>
<!--     
        <start>2007-06-10 12:07:00</start>
        <complete>2007-06-10 13:59:00</complete> 
-->

    </generalProperties>
    
    <groups>        
        <agents>
            <logicalGroup name="AG1" max="17">
                <conditions>             
                    <condition param="AgentIndex" relation="is">
                    <!--<values>12738</values> -->
                    <values>11908</values>
                    </condition>
                    
                </conditions>
            </logicalGroup>
            
        </agents>
        
        
        <dests>
        
            <fixedGroup name="DG1" max="10"> 
           132.65.240.105 132.65.240.106 130.206.163.166 130.206.163.165
<!-- 	132.65.240.105  - ping OK  -->           
<!-- 	132.65.240.106  - NO ping  -->           
<!-- 	130.206.163.166 - NO ping  -->           
<!-- 	130.206.163.165 - ping OK -->           	
            </fixedGroup>
            
        </dests>
        
    </groups>
    
    
    <mapping>
    </mapping>
    
      
    <qbeMapping>
        <etomic-listen-period-minutes>7</etomic-listen-period-minutes>
        <qbeItem agentsGroup="AG1" >
            <traceroute destsGroup="DG1" />
            <qbe> 
    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [100] [UDP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>100</packetSize>
                    <protocol>UDP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>

    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [200] [UDP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>200</packetSize>
                    <protocol>UDP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>

    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [400] [UDP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>400</packetSize>
                    <protocol>UDP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>
    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [700] [UDP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>700</packetSize>
                    <protocol>UDP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>

    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [1400] [UDP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>1400</packetSize>
                    <protocol>UDP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>


    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [100] [ICMP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>100</packetSize>
                    <protocol>ICMP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>

    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [200] [ICMP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>200</packetSize>
                    <protocol>ICMP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>

    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [400] [ICMP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>400</packetSize>
                    <protocol>ICMP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>
    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [700] [ICMP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>700</packetSize>
                    <protocol>ICMP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>

    <!--    QBE-QPT 10 2000 100 [132.65.240.106] [1400] [ICMP] [128] [128] [7777] [80] -->
                <qpt robin="10" delay="2000" trainLength="100" waitAfter="1500" > 
                    <destIp>132.65.240.106</destIp>
                    <packetSize>1400</packetSize>
                    <protocol>ICMP</protocol>                 
                    <tos>0</tos>                
                    <ttl>128</ttl>                
                    <sourcePort>7777</sourcePort>                
                    <destPort>7777</destPort>                 
                </qpt>



            </qbe>
            
            <traceroute destsGroup="DG1" />
        </qbeItem>
        
    </qbeMapping>


</DIMES_experiment>
