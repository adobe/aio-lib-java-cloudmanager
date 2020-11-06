For the developer's sanity's sake, each API uses a different program.

#### Program to Tests Breakdown

Most of the tests will use a specific program to help delinate context. These help track those contexts.

| API |Program ID|
| :--- | -----------: |
|Environments|2|
|Pipelines|3|
|Executions|4|

#### Program Details:

|Program | Test State|
| :--- | ---: |
| 2 | Delete - Bad Request|
| 3 | Delete - Success |

#### Environments
|Program | Environment | Test State|
| :--- | --- | ---: |
| 1 | | Not Found |
| 3 | | Empty List |
| 2 | | List Success |
| 2 | 3 | Delete Fails - Bad Request |
| 2 | 12 | Delete Fails - Not Found <br/> Get Variable Fails |
| 2 | 1 | Delete Success <br/> Set Variables Success|
| 2 | 2 | Missing Variables Link |
| 2 | 3 | No Developer Console <br/> Get Variables Not Found <br/> Set Variables Bad Request |
| 2 | 4 | Variable List Empty <br/> Set Variable Empty List | 

#### Pipeline Details
|Program | Pipeline | Test State|
| :--- | --- | ---: |
| 1 | | Pipelines Not Found|
| 2 | | List Empty|
| 3 | | List Success |
| 3 | 1 | Start Success <br/> Patch Success <br/> Delete Success <br/> Variable successes |
| 3 | 2 | Start Fails - Running <br/> Missing Variables Link |
| 3 | 3 | Start Fails - Not Found <br/> Get Variables not found <br/> Set Variables Bad Request |
| 3 | 4 | Update Fails - Not Allowed <br/> Delete Fails - Bad Request <br/> Variable List Empty <br/> Set Variable Empty List |
| 3 | 10 | Pipeline Missing |

#### Execution Details:

| Pipeline | Execution| Test State | 
| :--- | :---: | ---: |
| 1 |  |  Not Running (404) |
| 2 | 1 | Current Running <br/> Cancel Current Running|
| 3 | 1 | Specific Running <br/> Cancel Specific Running <br> Cancel via Execution |
| 3 | 2 | Cancel/Advance - Code Quality |
| 3 | 3 | Cancel/Advance - Code Quality Invalid State |
| 3 | 4 | Cancel/Advance - Approval Waiting |
| 3 | 5 | Cancel - Deploy Waiting |
| 3 | 6 | Cancel - Deploy Invalid State |
| 3 | 7 | Advance - Build Running | 
| 4 | 1 | No Steps | 
| 5 | 1 | No Active Step |
| 10 |  | Execution Missing (404) |
