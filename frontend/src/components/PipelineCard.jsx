import React from 'react'

function PipelineCard({pipeline}) {
  return (
    <div className='bg-white shadow-md p-4 rounded-xl mb-4'>
        <h2 className='text-xl font-bold'>{pipeline.name}</h2>
      
    </div>
  )
}

export default PipelineCard
